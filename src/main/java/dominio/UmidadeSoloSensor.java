package dominio;

import java.util.Random;

public class UmidadeSoloSensor extends SensorSimulado {
    public UmidadeSoloSensor(String id, Random random) {
        super(id, TipoSensor.UMIDADE_SOLO, random, 45.0);
    }

    @Override
    protected double proximoValor(double anterior, boolean tempestade) {
        if (tempestade) {
            return anterior + 3 + random.nextDouble() * 5; // solo vai saturando
        }
        return anterior + (random.nextDouble() - 0.5) * 4;
    }
}
