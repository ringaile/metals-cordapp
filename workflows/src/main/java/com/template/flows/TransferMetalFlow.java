package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferMetalFlow extends FlowLogic<SignedTransaction> {

    //private variables
    private String metalName;
    private int weight;
    private Party newOwner;
    private int x = 0;

    public TransferMetalFlow(String metalName, int weight, Party newOwner) {
        this.metalName = metalName;
        this.weight = weight;
        this.newOwner = newOwner;
    }

    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary.");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty.");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.");


    private final ProgressTracker progressTracker = new ProgressTracker(
            RETRIEVING_NOTARY,
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            COUNTERPARTY_SESSION,
            FINALISING_TRANSACTION
    );

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }



    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        //Initiator flow logic goes here

        //Retrieve notary identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        StateAndRef<MetalState> inputState = checkForMetalStates();

        Party issuer = inputState.getState().getData().getIssuer();

        //Create transaction components
        MetalState outputState = new MetalState(metalName, weight, issuer, newOwner);
        Command command = new Command(new MetalContract.Commands.Transfer(), getOurIdentity().getOwningKey());

        //create trx builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txB = new TransactionBuilder(notary)
                .addInputState(inputState)
                .addOutputState(outputState, MetalContract.CID)
                .addCommand(command);

        //Sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txB);

        //Create session with Counterparty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession = initiateFlow(issuer);

        //Finalize and send to Counterparty
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow(signedTx, otherPartySession, mintPartySession));

    }

    private StateAndRef<MetalState> checkForMetalStates() throws FlowException{
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        List<StateAndRef<MetalState>> metalStates = getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();

        boolean inputFound = false;

        for (StateAndRef<MetalState> metalState: metalStates){
            if(metalState.getState().getData().getMetalName().equals(metalName)
            && metalState.getState().getData().getWeight() == weight) {
                this.x = x;
                inputFound = true;
            }

        }

        if(!inputFound)
            throw new FlowException("Metal State was not found.");

        return metalStates.get(this.x);
    }
}
