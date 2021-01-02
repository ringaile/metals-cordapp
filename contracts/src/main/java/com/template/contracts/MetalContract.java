package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class MetalContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String CID = "com.template.contracts.MetalContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException{

        if(tx.getCommands().size()!=1)
            throw new IllegalArgumentException("Transaction must have one Command.");

        Command command = tx.getCommand(0);
        CommandData commandData = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        // Issue Command Contract Rules
        if(commandData instanceof Commands.Issue) {
            //Issue transaction logic

            //Shape rules
            if(tx.getInputs().size()!=0)
                throw new IllegalArgumentException("Issue cannot have inputs");
            if(tx.getOutputs().size()!=1)
                throw new IllegalArgumentException("Issue can only have one output");

            //Content rules
            ContractState outputState = tx.getOutput(0);
            if(!(outputState instanceof MetalState))
                throw new IllegalArgumentException("Output must be a metal State");

            MetalState metalState = (MetalState) outputState;
            if(!metalState.getMetalName().equals("Gold")&&!metalState.getMetalName().equals("Silver"))
                throw new IllegalArgumentException("Metal is not silver or gold");

            //Signer Rules
            Party issuer = metalState.getIssuer();
            PublicKey issuerKey = issuer.getOwningKey();

            if(!(requiredSigners.contains(issuerKey)))
                throw new IllegalArgumentException("Issuer has to sign the issuance");
        }

        //Transfer Command Contract Rules
        else if(commandData instanceof Commands.Transfer) {
            //Issue transaction logic

            //Shape rules
            if(tx.getInputs().size()!=1)
                throw new IllegalArgumentException("Transfer can only have one inputs");
            if(tx.getOutputs().size()!=1)
                throw new IllegalArgumentException("Transfer can only have one output");

            //Content rules
            ContractState inputState = tx.getInput(0);
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof MetalState))
                throw new IllegalArgumentException("Output must be a metal State");

            MetalState metalState = (MetalState) inputState;
            if(!metalState.getMetalName().equals("Gold")&&!metalState.getMetalName().equals("Silver"))
                throw new IllegalArgumentException("Metal is not silver or gold");

            //Signer Rules
            Party owner = metalState.getOwner();
            PublicKey ownerKey = owner.getOwningKey();

            if(!(requiredSigners.contains(ownerKey)))
                throw new IllegalArgumentException("Owner has to sign the issuance");
        }
        else throw new IllegalArgumentException("Unrecognised command");

    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Issue implements Commands {}
        class Transfer implements Commands {}
    }
}