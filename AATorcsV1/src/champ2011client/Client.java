/**
 * 
 */
package champ2011client;

import java.util.ArrayList;
import java.util.StringTokenizer;

import QLearning.*;
import champ2011client.Controller.Stage;

/**
 * @author Daniele Loiacono
 * 
 */
public class Client {

	private static int UDP_TIMEOUT = 10000;
	private static int port;
	private static String host;
	private static String clientId;
	private static boolean verbose;
	private static int maxEpisodes;
	private static int maxSteps;
	private static Stage stage;
	private static String trackName;

	
	private static QTable qtable;
	
	/**
	 * @param args
	 *            is used to define all the options of the client.
	 *            <port:N> is used to specify the port for the connection (default is 3001)
	 *            <host:ADDRESS> is used to specify the address of the host where the server is running (default is localhost)  
	 *            <id:ClientID> is used to specify the ID of the client sent to the server (default is championship2009) 
	 *            <verbose:on> is used to set verbose mode on (default is off)
	 *            <maxEpisodes:N> is used to set the number of episodes (default is 1)
	 *            <maxSteps:N> is used to set the max number of steps for each episode (0 is default value, that means unlimited number of steps)
	 *            <stage:N> is used to set the current stage: 0 is WARMUP, 1 is QUALIFYING, 2 is RACE, others value means UNKNOWN (default is UNKNOWN)
	 *            <trackName:name> is used to set the name of current track
	 */
	public static void main(String[] args) {
		parseParameters(args);
		SocketHandler mySocket = new SocketHandler(host, port, verbose);
		String inMsg;

		Controller driver = load(args[0]);
		driver.setStage(stage);
		driver.setTrackName(trackName);
		
		/* Build init string */
		float[] angles = driver.initAngles();
		String initStr = clientId + "(init";
		for (int i = 0; i < angles.length; i++) {
			initStr = initStr + " " + angles[i];
		}
		initStr = initStr + ")";
		
		long curEpisode = 0;
		boolean shutdownOccurred = false;
		
		/**
		 * 		Q-TABLE
		 */
		// QTable(numEstados);
		qtable = new QTable(Constantes.NUM_STATES_STEER); // Numero de estados de giro de volante (3).
		QTableFrame qTableFrame = new QTableFrame(qtable);
		
		do {

			/*
			 * Client identification
			 */

			do {
				mySocket.send(initStr);
				inMsg = mySocket.receive(UDP_TIMEOUT);
			} while (inMsg == null || inMsg.indexOf("***identified***") < 0);

			/*
			 * Start to drive
			 */
			long currStep = 0;
			while (true) {
				/*
				 * Receives from TORCS the game state
				 */
				inMsg = mySocket.receive(UDP_TIMEOUT);

				if (inMsg != null) {

					/*
					 * Check if race is ended (shutdown)
					 */
					if (inMsg.indexOf("***shutdown***") >= 0) {
						shutdownOccurred = true;
						System.out.println("Server shutdown!");
						break;
					}

					/*
					 * Check if race is restarted
					 */
					if (inMsg.indexOf("***restart***") >= 0) {
						driver.reset();
						if (verbose)
							System.out.println("Server restarting!");
						break;
					}

					Action action = new Action();
					if (currStep < maxSteps || maxSteps == 0)
						action = driver.control(new MessageBasedSensorModel(
								inMsg));
					else
						action.restartRace = true;

					currStep++;
					mySocket.send(action.toString());
				} else
					System.out.println("Server did not respond within the timeout");
			}

		} while (++curEpisode < maxEpisodes && !shutdownOccurred);

		/*
		 * Shutdown the controller
		 */
		driver.shutdown();
		mySocket.close();
		System.out.println("Client shutdown.");
		System.out.println("Bye, bye!");

	}
	
	public void train(Integer nmEpisodes, Integer startState, Integer targetState, Double porcentaje)
			throws InterruptedException {
				
		double p = porcentaje;
		// Por cada episodio
		for (Integer episodes = 0; episodes < nmEpisodes; episodes++) {

			porcentaje = p + (1 - p) * (((double) episodes / (double) (nmEpisodes-50)));
			if (porcentaje > 1.0)
				porcentaje = 1.0;
			
			System.out.println(porcentaje);
			double recompensa_acumulada = 0;
			int long_camino = 0;

			Integer currentState = startState;

			long inicio = System.nanoTime();
			// Mientras no se llegue al estado objetivo
			while (!currentState.equals(targetState)) {

				// Thread.sleep(1);

				// Paso 1. Escoger un movimiento.

				// Elige la posición que obtenga una mayor recompensa a partir del estado
				// actual. //EXPLOTA
				MovePosition movePosition = qtable.getBestRewardPosition(currentState, new ArrayList<MovePosition>());

				// Calculamos el estado siguiente
				Integer nextState = null;
				// Explora nuevos estados
				if (this.randomGenerator.nextDouble() >= porcentaje) { // EXPLORA
					// Elige un movimiento aleatorio
					MovePosition sorted = MovePosition.values()[this.randomGenerator.nextInt(4)];
					movePosition = sorted;
				}
				nextState = maze.move(currentState, movePosition);

				// Si el estado siguiente se sale del mapa se le recompensa con una puntuación muy baja.
				
				if (nextState == -1) {
					nextState = currentState;
					// Obtiene las coordenadas del estado siguiente
					Integer[] targetCoordinates = maze.getCoordinates(nextState);

					// Obtiene la recompensa del destino
					Double targetReward = Double.MIN_VALUE;

					// Step 2 (Sets Q-Table reward and move
					// Paso 2. Establece la recompensa y el movimiento en la Q-tabla
					Double reward = qtable.setReward(currentState, nextState, movePosition, targetReward,
							getBestMoveFromTarget(nextState), episodes);

					recompensa_acumulada += qtable.getReward(currentState, movePosition);

				} else {

					// Obtiene las coordenadas del estado siguiente
					Integer[] targetCoordinates = maze.getCoordinates(nextState);

					// Obtiene la recompensa del destino
					Double targetReward = maze.getMap()[targetCoordinates[0]][targetCoordinates[1]] * 1.0;

					// Step 2 (Sets Q-Table reward and move
					// Paso 2. Establece la recompensa y el movimiento en la Q-tabla
					Double reward = qtable.setReward(currentState, nextState, movePosition, targetReward,
							getBestMoveFromTarget(nextState), episodes);

				}
				recompensa_acumulada += qtable.getReward(currentState, movePosition);

				// Actualiza la ventana de la Q-Tabla

				this.qTableFrame.setQTable(qtable);

				// Actualiza el estado actual.
				currentState = nextState;
				long_camino++;
			}

			long fin = System.nanoTime();
			//////////////////////////////////////

			System.out.println("% Explotación: " + porcentaje);
			System.out.println("Iteracion " + episodes + ":" + (double) ((fin - inicio)) / 100000);
			System.out.println("Recompensa" + episodes + "=" + (recompensa_acumulada));
			System.out.println("Longitud Camino = " + long_camino);
			System.out.println("-----------------------------");

		}

	}
	
	

	private static void parseParameters(String[] args) {
		/*
		 * Set default values for the options
		 */
		port = 3001;
		host = "localhost";
		clientId = "championship2011";
		verbose = false;
		maxEpisodes = 1;
		maxSteps = 0;
		stage = Stage.UNKNOWN;
		trackName = "unknown";
		
		for (int i = 1; i < args.length; i++) {
			StringTokenizer st = new StringTokenizer(args[i], ":");
			String entity = st.nextToken();
			String value = st.nextToken();
			if (entity.equals("port")) {
				port = Integer.parseInt(value);
			}
			if (entity.equals("host")) {
				host = value;
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("verbose")) {
				if (value.equals("on"))
					verbose = true;
				else if (value.equals(false))
					verbose = false;
				else {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("id")) {
				clientId = value;
			}
			if (entity.equals("stage")) {
				stage = Stage.fromInt(Integer.parseInt(value));
			}
			if (entity.equals("trackName")) {
				trackName = value;
			}
			if (entity.equals("maxEpisodes")) {
				maxEpisodes = Integer.parseInt(value);
				if (maxEpisodes <= 0) {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
			if (entity.equals("maxSteps")) {
				maxSteps = Integer.parseInt(value);
				if (maxSteps < 0) {
					System.out.println(entity + ":" + value
							+ " is not a valid option");
					System.exit(0);
				}
			}
		}
	}

	private static Controller load(String name) {
		Controller controller=null;
		try {
			controller = (Controller) (Object) Class.forName(name)
					.newInstance();
			System.out.println(Class.forName(name));
		} catch (ClassNotFoundException e) {
			System.out.println(name	+ " is not a class name");
			System.exit(0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controller;
	}
}
