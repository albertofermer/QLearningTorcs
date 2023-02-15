# Aprendizaje por Refuerzo. 
## Algoritmo Q-Learning aplicado a TORCS

El algoritmo Q-Learning es un algoritmo de aprendizaje por refuerzo que permite que un agente aprenda acciones que maximicen una recompensa. 
En estre trabajo hemos implementado dicho algoritmo para que nuestro agente aprenda a manejar tanto el volante como el acelerador y freno del simulador TORCS.

### Modificación del algoritmo original
Para implementar el algoritmo hemos tenido que modificar el Q-Learning original para que, en lugar de trabajar con el estado actual y el estado futuro, trabaje con el estado anterior y el estado actual.
De esta manera podemos actualizar las recompensas en función de estados ya conocidos, sin necesidad de predecir el futuro.


![image](https://user-images.githubusercontent.com/106925084/219126131-77f2c69c-6826-4f28-acce-a579f5b6f612.png)

### Entrenamiento de la dirección
Para esta Q-Table hemos decidido generar 15 estados. Cada estado estará definido por:
+ La posición del coche sobre la carretera: habrá 3 posibles posiciones.
  + Izquierda
  + Centro
  + Derecha
+ El ángulo del coche con el eje principal de la carretera: Por cada posición anterior, habrá 5 posibles ángulos.

![image](https://user-images.githubusercontent.com/106925084/219127985-b0e17a71-2b8f-4435-a1d1-9619763a6498.png)


Las acciones que hemos decidido han sido los diferentes ángulos de giro del volante (radianes):


![image](https://user-images.githubusercontent.com/106925084/219128254-8583b66b-63b9-4a5f-aca0-fec9beef57e4.png)


Para calcular la recompensa hemos tenido en cuenta 2 escenarios:
+ Si el coche se ha salido de la carretera: $$-10000*AngleToTrackAxis()$$

+ Si el coche no se ha salido: $$(\frac{1}{1+|trackPosition()|})^4\cdot 0.7 + (\frac{1}{1+|AngleToTrackAxis()|} ) ^4 \cdot 0.3$$

Los resultados son los siguientes:


![image](https://user-images.githubusercontent.com/106925084/219129923-d88a8c01-3fe9-4a4f-976a-1c35b39ccf85.png)


### Entrenamiento de la velocidad
Para entrenar la velocidad hemos cogido 11 estados generados a partir del sensor que proporciona TORCS (getTrackEdgeSensors()[9]). Hemos discretizado sus valores de 20 en 20 generando 10 estados. El estado que falta corresponde con el valor -1, que genera el sensor cuando el coche se sale de la pista.

En este caso las acciones serán una tupla de dos elementos: (acelerador,freno). El uso de cada uno es exclusivo, es decir, si está acelerando no puede frenar y viceversa.
Las acciones son las siguientes: 

![image](https://user-images.githubusercontent.com/106925084/219130471-ad4824f9-fb44-4daa-9e9a-54f76abe7d4c.png)

En cuanto a las recompensas, hemos contemplado 3 casos:
+ Si se sale de la carretera: -10000 puntos
+ Si pasa más de 10000 ticks: -10000 puntos
+ En otro caso: $$(\frac{1}{1+|trackPosition()|})^4\cdot 0.7 + \frac{getSpeed()}{200} \cdot 0.3$$


Curiosamente, la política generada es la siguiente:

![image](https://user-images.githubusercontent.com/106925084/219130902-bc80209c-3375-4663-aeb0-9c829a7b490b.png)


Cabe señalar que el agente decide no frenar en ningún momento, en lugar de frenar prefiere desacelerar para no perder velocidad. Esto puede llegar a ser un problema en otros
circuitos, ya que las curvas pueden ser más cerradas. Para solucionar esto deberíamos entrenarlo con otros circuitos.

Los resultados de este agente son los siguientes:

![image](https://user-images.githubusercontent.com/106925084/219131416-f1286bbb-7cd0-44a8-87cf-f68471da0882.png)


