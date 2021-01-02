package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SearchVault extends FlowLogic<Void> {

    @Suspendable
    @Override
    public Void call() throws FlowException {

        searchForAllStates();

        return null;
    }

    private void searchForAllStates() {
        //Search for CONSUMED States
        QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        List<StateAndRef<MetalState>> consumedMetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, consumedCriteria).getStates();

        if(consumedMetalStates.size() < 1) {
            System.out.println("No CONSUMED Metal States found");
        } else {
            System.out.println("Total CONSUMED Metal States found: " +consumedMetalStates.size());
        }

        for(StateAndRef<MetalState> metalState: consumedMetalStates) {
            System.out.print("\n Name: " + metalState.getState().getData().getMetalName());
            System.out.print(" Owner: " + metalState.getState().getData().getOwner());
            System.out.print(" Weight: " + metalState.getState().getData().getWeight());
            System.out.print(" Issuer: " + metalState.getState().getData().getIssuer());
        }


        //Search for UNCONSUMED States
        QueryCriteria unconsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<MetalState>> unconsumedMetalStates = getServiceHub().getVaultService().queryBy(MetalState.class, unconsumedCriteria).getStates();

        if(unconsumedMetalStates.size() < 1) {
            System.out.println("No UNCONSUMED Metal States found");
        } else {
            System.out.println("Total UNCONSUMED Metal States found: " +unconsumedMetalStates.size());
        }

        for(StateAndRef<MetalState> metalState: unconsumedMetalStates) {
            System.out.print("\n Name: " + metalState.getState().getData().getMetalName());
            System.out.print(" Owner: " + metalState.getState().getData().getOwner());
            System.out.print(" Weight: " + metalState.getState().getData().getWeight());
            System.out.print(" Issuer: " + metalState.getState().getData().getIssuer());
        }
    }
}
