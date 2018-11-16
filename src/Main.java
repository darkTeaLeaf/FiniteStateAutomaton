import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        //Get input and create output file
        Scanner in = new Scanner(new File("fsa.txt"));
        PrintWriter writer = new PrintWriter(new FileWriter(new File("result.txt")));

        //Create arrays for input data
        ArrayList<String> states = new ArrayList<>();
        HashMap<String, ArrayList<Transition>> fsa = new HashMap<>();
        HashMap<String, ArrayList<Transition>> inTransitions = new HashMap<>();
        HashMap<String, ArrayList<String>> stateType = new HashMap<>();
        ArrayList<String> alphabet = new ArrayList<>();
        String initial = "";
        ArrayList<String> fin = new ArrayList<>();

        //Parsing the first string
        if (in.hasNext()) { //Check existence of string for E5
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("states=\\{((([a-z0-9]+,)*[a-z0-9]+)?)}",input)) {
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                //Get necessary data from string
                for (String s : input.split(",")) {
                    //Add state to array
                    states.add(s);
                    fsa.put(s, new ArrayList<>());
                    inTransitions.put(s, new ArrayList<>());
                    //Set default type of state
                    ArrayList<String> type = new ArrayList<>();
                    type.add("common");
                    stateType.put(s, type);
                }
            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        //Parsing the second string
        if (in.hasNext()) { //Check existence of string for E5
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("alpha=\\{((([a-z0-9_]+,)*[a-z0-9_]+)?)}",input)) {
                //Add element of alphabet to array
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                Collections.addAll(alphabet, input.split(","));
            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        //Parsing the third string
        if (in.hasNext()) {
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("init.st=\\{(([a-z0-9]+)?)}",input)) {
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                //Check existence of initial state for E4
                if (input.equals("")) {
                    E4(writer);
                }
                for (String s : input.split(",")) {
                    for (String key : stateType.keySet()) {
                        if (key.equals(s)) {
                            //Set new type of state
                            ArrayList<String> type = new ArrayList<>();
                            type.add("initial");
                            stateType.put(key, type);
                            initial = s;
                        }
                    }
                    //Check existence of state in states' set for E1
                    if (!s.equals("") && !fsa.containsKey(s)) {
                        E1(writer, s);
                    }
                }
            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        //Parsing the fourth string
        if (in.hasNext()) {
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("fin.st=\\{((([a-z0-9]+,)*[a-z0-9]+)?)}",input)) {
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                for (String s : input.split(",")) {
                    for (String key : fsa.keySet()) {
                        if (key.equals(s)) {
                            //For initial state set another one type (in case initial final state)
                            if (stateType.get(key).contains("initial")) {
                                stateType.get(key).add("final");
                            } else {
                                //Set new type of state
                                ArrayList<String> type = new ArrayList<>();
                                type.add("final");
                                stateType.put(key, type);
                            }
                            fin.add(s);
                        }
                    }
                    //Check existence of state in states' set for E1
                    if (!s.equals("") && !fsa.containsKey(s)) {
                        E1(writer, s);
                    }
                }
            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        //Parsing the fourth string
        if (in.hasNext()) {
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("trans=\\{((((([a-z0-9_]+)>){2}[a-z0-9]+),)*((([a-z0-9_]+)>){2}[a-z0-9]+))?}", input)) {
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                for (String s : input.split(",")) {
                    //Create new transition from input data
                    Transition transition = new Transition(s.substring(0, s.indexOf('>')),
                            s.substring(s.indexOf('>') + 1, s.lastIndexOf('>')),
                            s.substring(s.lastIndexOf('>') + 1, s.length()));

                    for (String key : fsa.keySet()) {
                        //Add transition to array with transitions which come out of state
                        if (key.equals(s.substring(0, s.indexOf('>')))) {
                            fsa.get(key).add(transition);
                        }
                        //Add transition to array with transitions which come in state
                        if (key.equals(s.substring(s.lastIndexOf('>') + 1, s.length()))) {
                            inTransitions.get(key).add(transition);
                        }
                    }

                    //Check existence of the first transition's state in states' set for E1
                    if (!s.equals("") && !fsa.containsKey(s.substring(0, s.indexOf('>')))) {
                        E1(writer, s.substring(0, s.indexOf('>')));
                    }
                    //Check existence of the second transition's state in states' set for E1
                    if (!s.equals("") && !fsa.containsKey(s.substring(s.lastIndexOf('>') + 1, s.length()))) {
                        E1(writer, s.substring(s.lastIndexOf('>') + 1, s.length()));
                    }
                    //Check existence of the transition in transitions' set for E3
                    if (!alphabet.contains(s.substring(s.indexOf('>') + 1, s.lastIndexOf('>')))) {
                        E3(writer, s.substring(s.indexOf('>') + 1, s.lastIndexOf('>')));
                    }
                }
                //Check existence of disjoint states
                E2(writer, fsa, states);

            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        E6(writer, fsa, alphabet);
        KleeneAlgorithm algorithm = new KleeneAlgorithm();
        writer.write(algorithm.findRegExp(states,fsa, initial, fin));

        in.close();
        writer.close();
    }

    //Print errors
    private static void E1(PrintWriter writer, String state) {
        writer.write("Error:\nE1: A state \'" + state + "\' is not in set of states");
        writer.close();
        System.exit(0);
    }

    private static void E2(PrintWriter writer, HashMap<String, ArrayList<Transition>> fsa,
                           ArrayList<String> states) {
        ArrayList<String> states2 = new ArrayList<>(states);
        Stack<String> stack = new Stack<>();
        ArrayList<String> join = new ArrayList<>();
        stack.push(states2.get(0));
        String pop;
        while (!stack.empty()){
            pop = stack.pop();
            for (int i = 0; i < fsa.get(pop).size(); i++) {
                if(!fsa.get(pop).get(i).start.equals(fsa.get(pop).get(i).end) &&
                        !join.contains(fsa.get(pop).get(i).end)) {
                    stack.push(fsa.get(pop).get(i).end);
                }
            }
            join.add(pop);
        }

        for (int i = 0; i < states2.size(); i++) {
            for (String aJoin : join) {
                if (states2.get(i).equals(aJoin)) {
                    states2.remove(i);
                }
            }
        }

        if(!states2.isEmpty()){
            writer.write("Error:\nE2: Some states are disjoint");
            writer.close();
            System.exit(0);
        }

    }

    private static void E3(PrintWriter writer, String transition) {
        writer.write("Error:\nE3: A transition \'" + transition + "\' is not represented in the alphabet");
        writer.close();
        System.exit(0);
    }

    private static void E4(PrintWriter writer) {
        writer.write("Error:\nE4: Initial state is not defined");
        writer.close();
        System.exit(0);
    }

    private static void E5(PrintWriter writer) {
        writer.write("Error:\nE5: Input file is malformed");
        writer.close();
        System.exit(0);
    }

    private static void E6(PrintWriter writer, HashMap<String, ArrayList<Transition>> fsa, ArrayList<String> alphabet) {
        for (String key : fsa.keySet()) {
            //Check existence of transitions more than in alphabet for each state
            if (fsa.get(key).size() > alphabet.size()) {
                writer.write("Error:\nE6: FSA is nondeterministic");
                writer.close();
                System.exit(0);
            }
        }
    }

}

//Class for representation of transitions

class Transition {
    String start;
    String end;
    String letter;

    Transition(String start, String letter, String end) {
        this.start = start;
        this.end = end;
        this.letter = letter;
    }
}
