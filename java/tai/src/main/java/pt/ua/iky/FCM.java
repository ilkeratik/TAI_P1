package pt.ua.iky;

import static java.util.logging.Level.INFO;
import static pt.ua.iky.Common.readFile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FCM {

  private static final Logger log = Logger.getLogger(FCM.class.getName());

  public double runFcm(double alpha, int k, String fileName, boolean verbose) {
    String content = readFile(fileName);
    if (content == null || content.isEmpty()) {
      return 0d;
    }
    Set<Character> alphabet = new LinkedHashSet<>();
    for (char c : content.toCharArray()) {
      alphabet.add(c);
    }

    Map<String, Map<Character, Integer>> contextCounts = new HashMap<>(); //"CONTEXT" -> {"A": 1, "B":2}
    for (int i = 0; i + k < content.length(); i++) {
      String context = content.substring(i, i + k);
      Character nextChar = content.charAt(i + k);
      // Initialize maps if necessary
      contextCounts.putIfAbsent(context, new HashMap<>());
      Map<Character, Integer> charCounts = contextCounts.get(context);
      // Increment count for this character
      charCounts.put(nextChar, charCounts.getOrDefault(nextChar, 0) + 1);
    }

    // Compute the weighted sum of log probabilities for each (context, nextChar) pair:
    // count * log((count + alpha)/(totalCount_for_context + alpha*alphabet.size()))
    double sumLogProb = 0.0;
    int totalPredictions = content.length() - k;  // total number of predictions made

    for (Map.Entry<String, Map<Character, Integer>> entry : contextCounts.entrySet()) {
      Map<Character, Integer> counts = entry.getValue();
      // Sum of counts of the context
      int contextTotalCount = counts.values().stream().mapToInt(Integer::intValue).sum();
      // Denominator: observed count plus smoothing mass for every character
      double denominator = contextTotalCount + alpha * alphabet.size();

      // Iterate over the entire alphabet; if a character was not observed, its count is 0.
      for (Character c : alphabet) {
        int count = counts.getOrDefault(c, 0);
        // Only add if count is > 0
        if (count != 0) {
          double probability = (count + alpha) / denominator;
          sumLogProb += count * Math.log(probability);
        }
      }
    }

    // Average (per prediction) negative log probability converted into bits.
    double entropy = -sumLogProb / totalPredictions / Math.log(2);

    if (verbose) {
      for (Map.Entry<String, Map<Character, Integer>> entry : contextCounts.entrySet()) {
        log.info("Ctx: " + entry.getKey() + " -> " + entry.getValue());
      }
    }

    log.log(INFO, "Entropy: {0} bps", entropy);
    return entropy;
  }
}
