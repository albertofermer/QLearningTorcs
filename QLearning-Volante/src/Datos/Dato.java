package Datos;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import QLearning.Constantes;

public class Dato {

	private ArrayList<Double> recompensa_acumulada;
	private ArrayList<Integer> ticks_duracion;
	private ArrayList<Double> longitud_recorrida;
	private ArrayList<Float> angulos_volante;
	private ArrayList<Double> posicion_carretera;
	private ArrayList<Double> epsilon;
	private ArrayList<Double> distancia_meta;
	private ArrayList<Integer> indice_carrera;

	public Dato() {
		recompensa_acumulada = new ArrayList<>();
		ticks_duracion = new ArrayList<>();
		longitud_recorrida = new ArrayList<>();
		angulos_volante = new ArrayList<>();
		posicion_carretera = new ArrayList<>();
		epsilon = new ArrayList<>();
		distancia_meta = new ArrayList<>();
		indice_carrera = new ArrayList<>();
	}

	public void addRecompensa(Double recompensa) {
		this.recompensa_acumulada.add(recompensa);
	}

	public void addTick(Integer tick) {
		this.ticks_duracion.add(tick);
	}

	public void addLongitudRecorrida(Double longitud) {
		this.longitud_recorrida.add(longitud);
	}

	public void addAnguloVolante(float steer_angle) {
		this.angulos_volante.add(steer_angle);
	}

	public void addTrackPosition(Double tp) {
		this.posicion_carretera.add(tp);
	}

	public void addEpsilon(Double epsilon) {
		this.epsilon.add(epsilon);
	}
	
	public void addDistanciaMeta(Double dist_goal) {
		this.distancia_meta.add(dist_goal);
	}

	public void addIndiceCarrera(Integer index) {
		indice_carrera.add(index);
	}
	
	public void writeHeader(String file_name) {
		String str = "#CARRERA;TICK;STEER_ANGLE;TRACK_POSITION;EPSILON;DIST_RACED;DIST_FROM_START_LINE;\n";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file_name+".csv"));
			writer.append(str);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String file_name) {
		// Escribe los angulos del volante en columnas para representarlo posteriormente
		String str = "";
		for(int i = 0; i<angulos_volante.size(); i++) {
			str += indice_carrera.get(i) + ";";
			str += i + ";"; // Tick
			str += angulos_volante.get(i) + ";";
			str += posicion_carretera.get(i) + ";";
			str += epsilon.get(i) + ";";
			str += longitud_recorrida.get(i) + ";";
			str += distancia_meta.get(i) + ";";
			str += "\n"; // Salto de linea
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file_name+".csv",true));
			writer.append(str);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
