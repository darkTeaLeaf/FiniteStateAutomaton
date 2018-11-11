import java.util.ArrayList;
import java.util.HashMap;

public class KleeneAlgorithm {
    private HashMap<String, String> stepsFirst;
    private HashMap<String, String> stepsSecond;
    private ArrayList<String> states;

    public String findRegExp(ArrayList<String> states, HashMap<String, ArrayList<Transition>> fsa,
                           HashMap<String, ArrayList<String>> stateType) {
        stepsFirst = new HashMap<>();
        stepsSecond = new HashMap<>();
        this.states = states;
        int initial = -1;
        int fin = -1;

        for(String key: stateType.keySet()){
            for (int i = 0; i < stateType.get(key).size(); i++) {
                if(stateType.get(key).get(i).equals("initial")){
                    for (int j = 0; j < states.size(); j++) {
                        if(states.get(j).equals(key)){
                            initial = j;
                        }
                    }
                }
                if(stateType.get(key).get(i).equals("final")){
                    for (int j = 0; j < states.size(); j++) {
                        if(states.get(j).equals(key)){
                            fin = j;
                        }
                    }
                }
            }
        }

        initialRegExp(fsa);
        mainAlgorithm();
        if(states.size()%2 == 0){
            if (initial != -1 && fin != -1) {
                return stepsFirst.get(String.valueOf(initial) + String.valueOf(fin));
            }
            else {
                return "{}";
            }
        }
        else {
            if (initial != -1 && fin != -1) {
                return stepsSecond.get(String.valueOf(initial) + String.valueOf(fin));
            }
            else {
                return "{}";
            }
        }
    }

    private void initialRegExp(HashMap<String, ArrayList<Transition>> fsa) {
        boolean flag = false;
        for (int i = 0; i < states.size(); i++) {
            ArrayList<Transition> stateTransitions = fsa.get(states.get(i));
            for (int k = 0; k < states.size(); k++) {
                for (int j = 0; j < stateTransitions.size(); j++) {
                    if (stateTransitions.get(j).end.equals(states.get(k))) {
                        if (i == k) {
                            if(stepsFirst.get(String.valueOf(i) + String.valueOf(k))== null) {
                                stepsFirst.put(String.valueOf(i) + String.valueOf(k), stateTransitions.get(j).letter + "|eps");
                                flag = true;
                            }
                            else{
                                stepsFirst.put(String.valueOf(i) + String.valueOf(k), stepsFirst.get(String.valueOf(i)
                                        + String.valueOf(k)).substring(0, stepsFirst.get(String.valueOf(i)
                                        + String.valueOf(k)).length() - 4) + "|" + stateTransitions.get(j).letter + "|eps");
                                flag = true;
                            }
                        } else {
                            if(stepsFirst.get(String.valueOf(i) + String.valueOf(k))== null) {
                                stepsFirst.put(String.valueOf(i) + String.valueOf(k), stateTransitions.get(j).letter);
                                flag = true;
                            }
                            else {
                                stepsFirst.put(String.valueOf(i) + String.valueOf(k), stepsFirst.get(String.valueOf(i)
                                        + String.valueOf(k)) + "|" + stateTransitions.get(j).letter);
                                flag = true;
                            }
                        }
                    }
                }
                if (!flag) {
                    stepsFirst.put(String.valueOf(i) + String.valueOf(k), "{}");
                }
                flag = false;
            }
        }
    }

    private void mainAlgorithm() {
        for (int j = 0; j < states.size(); j++) {
            if (j % 2 == 0) {
                for (String key: stepsFirst.keySet()){
                    stepsSecond.put(key, "(" + stepsFirst.get(key.substring(0,1) + String.valueOf(j)) + ")"
                    + "(" + stepsFirst.get(String.valueOf(j) + String.valueOf(j)) + ")*"
                    + "(" + stepsFirst.get(String.valueOf(j) + key.substring(1)) + ")|"
                    + "(" + stepsFirst.get(key) + ")");
                }
            }
            else {
                for (String key: stepsSecond.keySet()){
                    stepsFirst.put(key, "(" + stepsSecond.get(key.substring(0,1) + String.valueOf(j)) + ")"
                            + "(" + stepsSecond.get(String.valueOf(j) + String.valueOf(j)) + ")*"
                            + "(" + stepsSecond.get(String.valueOf(j) + key.substring(1)) + ")|"
                            + "(" + stepsSecond.get(key) + ")");
                }
            }
        }
    }
}
