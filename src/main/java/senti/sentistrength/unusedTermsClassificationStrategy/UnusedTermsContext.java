package senti.sentistrength.unusedTermsClassificationStrategy;

import java.util.HashMap;
import java.util.Map;

public class UnusedTermsContext {
    private static final Map<String, UnusedTermsClassificationStrategy> Strategies = new HashMap<>();

    static {
        Strategies.put("Binary", new Binary());
        Strategies.put("Trinary", new Trinary());
        Strategies.put("Scale", new Scale());
        Strategies.put("PosNeg", new PosNeg());
    }

    /**
     * 策略模式的工厂创建对象，通过字段Type来获取对象.
     *
     * @param Type
     * @return
     */
    public static UnusedTermsClassificationStrategy getStrategy(String Type) {
        if (Type == null) {
            throw new IllegalArgumentException("pay type is empty.");
        }
        if (!Strategies.containsKey(Type)) {
            throw new IllegalArgumentException("pay type not supported.");
        }
        return Strategies.get(Type);
    }
}
