package dominio;

import java.util.Random;

public class PluviometroSensor extends SensorSimulado {
    public PluviometroSensor(String id, Random random) {
        super(id, TipoSensor.CHUVA, random, 2.0);
    }

    @Override
    protected double proximoValor(double anterior, boolean tempestade) {
        if (tempestade) {
            return anterior + 4 + random.nextDouble() * 9; // chuva que só intensifica
        }
        return anterior * 0.6 + random.nextDouble() * 3; // chuva fraca que se dissipa
    }
}
