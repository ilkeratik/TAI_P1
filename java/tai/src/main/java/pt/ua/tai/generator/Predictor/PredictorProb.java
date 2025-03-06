package pt.ua.tai.generator.Predictor;

import java.util.Map;
import java.util.Random;
import pt.ua.tai.generator.Predictor.ContextSearcher.ContextSearcher;

public class PredictorProb extends Predictor {

  private Random random;

  public PredictorProb(ContextSearcher contextSearcher, Map<String, Map<Character, Integer>> table,
      int responseSize, int k) {
    super(contextSearcher, responseSize, k);
    setTable(table);
    random = new Random();
  }

  public PredictorProb(ContextSearcher contextSearcher, Map<String, Map<Character, Integer>> table,
      int responseSize, int k, int seed) {
    super(contextSearcher, responseSize, k);
    setTable(table);
    random = new Random(seed);
  }

  @Override
  protected char chooseChar() {
    Map<Character, Integer> tempProb = table.get(context);
    int sum = tempProb.values().stream().reduce(0, Integer::sum);
    int randomValue = random.nextInt(sum);
    int cumulativeSum = 0;

    for (Map.Entry<Character, Integer> entry : tempProb.entrySet()) {
      cumulativeSum += entry.getValue();
      if (randomValue < cumulativeSum) {
        return entry.getKey();
      }
    }
    return ' ';
  }
}
