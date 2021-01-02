package com.template.contracts;

import com.template.states.MetalState;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;
import static net.corda.testing.node.NodeTestUtils.transaction;

public class ContractTests {
    private final MockServices ledgerServices = new MockServices();

    private Party Mint = new TestIdentity(new CordaX500Name("mint","","GB")).getParty();
    private Party TraderA = new TestIdentity(new CordaX500Name("trader","","GB")).getParty();
    private Party TraderB = new TestIdentity(new CordaX500Name("trader","","GB")).getParty();

    private MetalState metalState = new MetalState("Gold", 10, Mint, TraderA);

    private MetalState metalStateInput = new MetalState("Gold", 10, Mint, TraderA);
    private MetalState metalStateOutput = new MetalState("Gold", 10, TraderA, TraderB);


    @Test
    public void metalContractImplementsContract() {
        assert(new MetalContract() instanceof Contract);
    }

    // Issue commands

    @Test
    public void metalContractRequiresZeroInputsInIssueTrx() {
        transaction(ledgerServices, tx -> {
            //Has an input, will fail
            tx.input(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Does not have input, will verify
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresOneOutputInIssueTrx() {
        transaction(ledgerServices, tx -> {
            //Has two outputs, will fail
            tx.output(MetalContract.CID, metalState);
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Has one output, will verify
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheTrxOutputToBeOfMetalState() {
        transaction(ledgerServices, tx -> {
            //Has wrong output state, will fail
            tx.output(MetalContract.CID, new DummyState());
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Has correct output state, will verify
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheTrxCommandToBeAnIssueCommand() {
        transaction(ledgerServices, tx -> {
            //Has wrong command, will fail
            tx.output(MetalContract.CID, new DummyState());
            tx.command(Mint.getOwningKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Has correct command, will verify
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheIssuerToBeASignerInTheTrx() {
        transaction(ledgerServices, tx -> {
            //Issuer is not a required signer, will fail
            tx.output(MetalContract.CID, new DummyState());
            tx.command(TraderA.getOwningKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Issuer is a required signer, will verify
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    // Transfer Commands

    @Test
    public void metalContractRequiresOneInputAndOneOutputInTransferTrx() {
        transaction(ledgerServices, tx -> {
            //Does not have an input, will fail
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Does not have an output, will fail
            tx.input(MetalContract.CID, metalStateInput);
            tx.command(TraderA.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Has an input and an output, will verify
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheTrxCommandToBeATransferCommand() {
        transaction(ledgerServices, tx -> {
            //Has wrong command, will fail
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getOwningKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //has a correct command, will verify
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void metalContractRequiresTheOwnerToBeArequiredSigner() {
        transaction(ledgerServices, tx -> {
            //Owner is not a required signer, will fail
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(Mint.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            //Owner is a required signer, will verify
            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getOwningKey(), new MetalContract.Commands.Transfer());
            tx.verifies();
            return null;
        });
    }
}