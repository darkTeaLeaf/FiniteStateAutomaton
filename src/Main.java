import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        //Get input and create output file
        Scanner in = new Scanner(new File("fsa.txt"));
        PrintWriter writer = new PrintWriter(new FileWriter(new File("result.txt")));

        //Create arrays for input data
        HashMap<String, ArrayList<Transition>> fsa = new HashMap<>();
        HashMap<String, ArrayList<Transition>> inTransitions = new HashMap<>();
        HashMap<String, ArrayList<String>> stateType = new HashMap<>();
        ArrayList<String> alphabet = new ArrayList<>();

        //Parsing the first string
        if (in.hasNext()) { //Check existence of string for E5
            String input = in.next();
            //Check form of string for E5
            if (Pattern.matches("states=\\{((([a-z0-9]+,)*[a-z0-9]+)?)}",input)) {
                input = input.substring(input.indexOf('{') + 1, input.length() - 1);
                //Get necessary data from string
                for (String s : input.split(",")) {
                    //Add state to array
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
                E2(writer, fsa, inTransitions);

            } else {
                E5(writer);
            }
        } else {
            E5(writer);
        }

        //Print report and warnings
        report(writer, alphabet, fsa);
        boolean warning1 = W1(writer, stateType);
        boolean warning2 = W2(writer, fsa, stateType, warning1);
        W3(writer, fsa, alphabet, warning1 || warning2);

        in.close();
        writer.close();
    }

    //Print errors
    private static void E1(PrintWriter writer, String state) {
        writer.write("Error:\nE1: A state \'" + state + "\' is not in set of states");
        writer.close();
        System.exit(0);
    }

    private static void E2(PrintWriter writer, HashMap<String, ArrayList<Transition>> fsa, HashMap<String,
            ArrayList<Transition>> inTransition) {
        boolean mark = false;

        for (String key : fsa.keySet()) {
            //Check existence of transitions except loop transitions in transitions which come out of state
            for (Transition transition: fsa.get(key)){
                if(!transition.end.equals(transition.start)){
                    mark = true;
                    break;
                }
            }
            //Check existence of transitions except loop transitions in transitions which come in state
            for (Transition transition: inTransition.get(key)){
                if(!transition.end.equals(transition.start)){
                    mark = true;
                    break;
                }
                else{
                    mark = false;
                }
            }
            //Print error
            if(!mark && fsa.size()!=1){
                writer.write("Error:\nE2: Some states are disjoint");
                writer.close();
                System.exit(0);
            }
            mark = false;
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

    private static void report(PrintWriter writer, ArrayList<String> alphabet,
                               HashMap<String, ArrayList<Transition>> fsa) {
        boolean mark = false;
        boolean end = false;

        //Go through alphabet and check existence of each letter for each state
        for (String letter : alphabet) {
            for (String key : fsa.keySet()) {
                for (Transition transition : fsa.get(key)) {
                    if (transition.letter.equals(letter)) {
                        mark = true;
                        break;
                    }
                }
                if (!mark) {
                    writer.write("FSA is incomplete");
                    end = true;
                    break;
                }
                mark = false;
            }
            if (end) {
                break;
            }
        }
        if (!end) {
            writer.write("FSA is complete");
        }
    }

    private static boolean W1(PrintWriter writer, HashMap<String, ArrayList<String>> stateType) {
        boolean mark = false;
        //Go through array of states' types and find or not final states
        for (String type : stateType.keySet()) {
            if (stateType.get(type).contains("final")) {
                mark = true;
            }
        }
        //Print warning
        if (!mark) {
            writer.write("\nWarning:\n");
            writer.write("W1: Accepting state is not defined");
        }
        return !mark;
    }

    private static boolean W2(PrintWriter writer, HashMap<String, ArrayList<Transition>> fsa,
                              HashMap<String, ArrayList<String>> stateType, boolean warning) {
        Stack<String> stack = new Stack<>();
        HashMap<String, Integer> colors = new HashMap<>();
        for (String state : fsa.keySet()) {
            colors.put(state, 0);
        }
        String initial = "";
        for (String key : stateType.keySet()) {
            if (stateType.get(key).contains("initial")) {
                initial = key;
                break;
            }
        }

        //DFS from initial state
        stack.push(initial);
        while (!stack.empty()) {
            String state = stack.pop();
            colors.put(state, 1); //Set 1 for state which is reachable from initial state
            for (Transition transition : fsa.get(state)) {
                if (colors.get(transition.end) == 0) {
                    stack.push(transition.end);
                }
            }
        }

        //Check existence of states with 0 (not 1 like in reachable states)
        for (String state : colors.keySet()) {
            if (colors.get(state) == 0) {
                if (!warning) {
                    writer.write("\nWarning:\n");
                } else {
                    writer.write("\n");
                }
                writer.write("W2: Some states are not reachable from initial state");
                return true;
            }
        }
        return false;
    }

    private static void W3(PrintWriter writer, HashMap<String, ArrayList<Transition>> fsa, ArrayList<String> alphabet, boolean warning) {
        for (String key : fsa.keySet()) {
            //Check existence of transitions more than in alphabet for each state
            if (fsa.get(key).size() > alphabet.size()) {
                if (!warning) {
                    writer.write("\nWarning:\n");
                } else {
                    writer.write("\n");
                }
                writer.write("W3: FSA is nondeterministic");
                break;
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
