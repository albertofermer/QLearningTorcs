package QLearning;

import java.util.List;
import java.util.Random;

// QTable para controlar el volante

public class QTable {

	QCell[] qTable;
	Random randomGenerator;

	public QTable(Integer maxPositions) {
		this.qTable = new QCell[maxPositions];
		for (int i = 0; i < maxPositions; i++) {
			qTable[i] = new QCell(Constantes.NUM_ANGLES); // hay 9 posibles �ngulos para controlar el volante
		}
		this.randomGenerator = new Random();
	}
	
	public Integer size() {
		return this.qTable.length;
	}

	public Double getReward(Integer source, int movement) {
		return this.qTable[source].getReward(movement);
	}

	/**
	 * Obtiene la posici�n con mayor recompensa a partir de la posici�n actual.
	 * @param estado
	 * @param blackList
	 * @return
	 */
	public int getBestRewardPosition(Integer estado) {
		// Escoge un movimiento aleatorio.
		int bestPosition = this.randomGenerator.nextInt(Constantes.NUM_ANGLES);
		// Obtenemos la recompensa actual del mejor movimiento.
		Double bestReward = this.getReward(estado, bestPosition);
		
		// Por cada accion posible.
		for (int accion = 0; accion < Constantes.NUM_ANGLES; accion++) {
			// Se obtiene la recompensa del estado siguiente
			Double reward = getReward(estado, accion);
			
			// Si la recompensa del movimiento es mejor que la del mejor movimiento calculado
			// anteriormente y el movimiento no est� contenido en la blacklist
			if (reward > bestReward) {
				// Se actualiza la mejor posici�n
				bestPosition = accion;
				// Se actualiza la recompensa
				bestReward = reward;
			}
		}
		return bestPosition;
	}

	/**
	 * Calcula el Q-valor
	 * @param estado
	 * @param target
	 * @param accion
	 * @param targetReward
	 * @param targetBestMove
	 * @return
	 */
	public Double setReward(Integer estado, Integer target, Integer accion, Double targetReward,
		Integer targetBestMove) {
		
		Double currentQ = this.getReward(estado, accion);
		Double maxFutureQ = this.getReward(target, targetBestMove);
		Double learningRate = 0.5;
		Double discountFactor = 0.1;
		
		currentQ = (1-learningRate)*currentQ + learningRate*(targetReward + discountFactor * maxFutureQ);

		this.qTable[estado].setReward(accion, currentQ);
		return currentQ;

	}


	
	public QCell getQCell(Integer position) {
		return this.qTable[position];
	}

}
