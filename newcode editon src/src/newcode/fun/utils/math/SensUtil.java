package newcode.fun.utils.math;

import static newcode.fun.utils.IMinecraft.mc;

public class SensUtil {

    /**
     * Вычисляет чувствительность для заданного угла поворота.
     *
     * @param rot Угол поворота.
     * @return Чувствительность.
     */
    public static float getSensitivity(float rot) {
        float gcdValue = getGCDValue();
        return getDeltaMouse(rot, gcdValue) * gcdValue;
    }

    /**
     * Возвращает значение GCD (Greatest Common Divisor) с учетом чувствительности мыши.
     *
     * @return Значение GCD.
     */
    public static float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    /**
     * Вычисляет GCD на основе чувствительности мыши.
     *
     * @return Значение GCD.
     */
    public static float getGCD() {
        float sensitivityFactor = (float) (mc.gameSettings.mouseSensitivity * 0.6 + 0.2);
        return sensitivityFactor * sensitivityFactor * sensitivityFactor * 8;
    }

    /**
     * Вычисляет изменение угла поворота мыши.
     *
     * @param delta     Изменение угла.
     * @param gcdValue  Значение GCD.
     * @return Изменение угла поворота мыши.
     */
    public static float getDeltaMouse(float delta, float gcdValue) {
        if (gcdValue == 0) {
            return 0;
        }
        return Math.round(delta / gcdValue);
    }

    /**
     * Возвращает случайное отклонение для чувствительности.
     * Это помогает сделать поведение бота менее предсказуемым.
     *
     * @return Случайное отклонение.
     */
    public static float getRandomSensitivityOffset() {
        return (float) (Math.random() * 0.1 - 0.05);
    }

    /**
     * Возвращает улучшенное значение чувствительности с учетом случайного отклонения.
     *
     * @param rot Угол поворота.
     * @return Улучшенное значение чувствительности.
     */
    public static float getImprovedSensitivity(float rot) {
        float sensitivity = getSensitivity(rot);
        return sensitivity + getRandomSensitivityOffset();
    }
}