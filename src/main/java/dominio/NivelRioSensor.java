package dominio;

import java.util.Random;

public class NivelRioSensor extends SensorSimulado {
    private static final double NIVEL_NORMAL = 1.2;

    public NivelRioSensor(String id, Random random) {
        super(id, TipoSensor.NIVEL_RIO, random, NIVEL_NORMAL);
    }

    @Override
    protected double proximoValor(double anterior, boolean tempestade) {
        if (tempestade) {
            return anterior + 0.2 + random.nextDouble() * 0.5; // sobe devagar, com inércia
        }
        return anterior + (NIVEL_NORMAL - anterior) * 0.2 + (random.nextDouble() - 0.5) * 0.1;
    }
}
