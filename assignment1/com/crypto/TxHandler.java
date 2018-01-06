package com.crypto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.crypto.Transaction.Input;
import com.crypto.Transaction.Output;

/**
 * - Transaction: predstavlja transakcijo scrooge coin. Vsebuje hash transakcije in seznamv vhodov in izhodov.
 * - Transaction.output: predstavlja ciljno destinacijo, vsota "denarja" in javni kljuc
 * - Transaction.Input: predstavlja ciljno destinacijo pretekle/prejsnje transakcije, in vsebuje
 * 						hash prejsnje transakcije, index izhoda, ki je znotraj prejsnje transakcije
 * 						ter podpis trenutne transakcije. 
 * - UTXO: nezapravljen transakcijski izhod. (npr, to kar ostane po nakupu) Vsebuje hash transakcije iz katere izhaja, in index izhoda znotraj
 * 			te transakcije. 
 * - UTXOPool: bazen vseh nezakljucenih transkacij. Hrani hash senzam vseh UTXO do svojih prvotnih transakcij (glavna knjiga). 
 * 				
 * 
 * - Da je vhod korekten/valid, je potrebno preveriti ali je podpis v njemu (hrani podpis trenutne/ponorne/ciljne transakcije) pravilen.
 *  To storimo tako, da preverimo podpis trenutne transakcije s javnim kljuèem, ki je v ciljni destinaciji prejšnje transakcije.
 * @author Dejan Ognjenoviæ
 *
 */
public class TxHandler {

	private UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is {@code utxoPool}. This should make a copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);

	}

	/**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
    	if (tx == null)
    		return false;
    	
    	// 1
    	for (int index = 0; index < tx.numOutputs(); ++index) {
    		UTXO utxo = new UTXO(tx.getHash(), index);
    		
    		if (!utxoPool.contains(utxo))
    			return false;
    	}
    	
    	// 2
    	for (int index = 0; index < tx.numInputs(); ++index) {
    		Input input = tx.getInput(index);
    		UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
    		
    		if (!utxoPool.contains(utxo)) return false;
    		
    		Output output = utxoPool.getTxOutput(utxo);
    		
    		if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(index), input.signature)) 
    			return false;
    	}

    	// 3 
    	Set<UTXO> uniqueUTXOs = new HashSet<>();
    	for (int index = 0; index < tx.numOutputs(); ++index) {
    		UTXO utxo = new UTXO(tx.getHash(), index);
    		
    		if (!uniqueUTXOs.contains(utxo))
    			uniqueUTXOs.add(utxo);
    		else
    			return false;
    		
    	}
    		
    	// 4
    	for (Output output : tx.getOutputs()) {
    		if (0.0 > output.value) {
    			return false;
    		}
    	}
		
    	// 5
    	double outputSum = 0.0, inputSum = 0.0;
    	
    	for (int index = 0; index < tx.numInputs(); ++index) {
    		Input input = tx.getInput(index);
    		Output output = utxoPool.getTxOutput(new UTXO(input.prevTxHash, input.outputIndex));
    		
    		inputSum += output.value;
    	}
    	for (Output output : tx.getOutputs()) {
    		outputSum += output.value;
    	}
    	
    	if (inputSum < outputSum) {
    		return false;
    	}
    	
    	return true;
    	
    }

	/**
	 * Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness, returning a
	 * mutually valid array of accepted transactions, and updating the current
	 * UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		//Arrays.stream(possibleTxs).
		List<Transaction> newList = new ArrayList<>();
		for (Transaction transaction : possibleTxs) {
			if(isValidTx(transaction)) {
				for (Transaction.Input input : transaction.getInputs()) {
					UTXO utxo = new UTXO(input.prevTxHash, 
							input.outputIndex);
					if (utxoPool.contains(utxo)) {
						utxoPool.addUTXO(utxo, transaction.getInput(0).);
					}
				}
				
				newList.add(transaction);
				
				
				utxoPool.addUTXO(utxo, transaction.getOutputs().get(0));
			}
		}
		
		
	}

}
