import java.util.*;

class Bank {
    public String name;
    public int netAmount;
    public Set<String> types = new HashSet<>();
}

class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }
}

public class CashFlowMinimizer {

    public static int getMinIndex(Bank[] listOfNetAmounts, int numBanks) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    public static int getSimpleMaxIndex(Bank[] listOfNetAmounts, int numBanks) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;

            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    public static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int numBanks, int minIndex, Bank[] input, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";

        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0 || listOfNetAmounts[i].netAmount < 0) continue;

            List<String> commonTypes = new ArrayList<>(listOfNetAmounts[minIndex].types);
            commonTypes.retainAll(listOfNetAmounts[i].types);

            if (!commonTypes.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = commonTypes.get(0);
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    public static void printAns(List<List<Pair<Integer, String>>> ansGraph, int numBanks, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows:\n");
        for (int i = 0; i < numBanks; i++) {
            for (int j = 0; j < numBanks; j++) {
                if (i == j) continue;

                if (ansGraph.get(i).get(j).getKey() != 0 && ansGraph.get(j).get(i).getKey() != 0) {
                    if (ansGraph.get(i).get(j).getKey().equals(ansGraph.get(j).get(i).getKey())) {
                        ansGraph.get(i).get(j).setKey(0);
                        ansGraph.get(j).get(i).setKey(0);
                    } else if (ansGraph.get(i).get(j).getKey() > ansGraph.get(j).get(i).getKey()) {
                        ansGraph.get(i).get(j).setKey(ansGraph.get(i).get(j).getKey() - ansGraph.get(j).get(i).getKey());
                        ansGraph.get(j).get(i).setKey(0);
                        System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).getKey() + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getValue());
                    } else {
                        ansGraph.get(j).get(i).setKey(ansGraph.get(j).get(i).getKey() - ansGraph.get(i).get(j).getKey());
                        ansGraph.get(i).get(j).setKey(0);
                        System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).getKey() + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getValue());
                    }
                } else if (ansGraph.get(i).get(j).getKey() != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph.get(i).get(j).getKey() + " to " + input[j].name + " via " + ansGraph.get(i).get(j).getValue());
                } else if (ansGraph.get(j).get(i).getKey() != 0) {
                    System.out.println(input[j].name + " pays Rs " + ansGraph.get(j).get(i).getKey() + " to " + input[i].name + " via " + ansGraph.get(j).get(i).getValue());
                }
                ansGraph.get(i).get(j).setKey(0);
                ansGraph.get(j).get(i).setKey(0);
            }
        }
        System.out.println();
    }

    public static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];
        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank();
            listOfNetAmounts[b].name = input[b].name;
            listOfNetAmounts[b].types = input[b].types;
            int amount = 0;

            for (int i = 0; i < numBanks; i++) {
                amount += graph[i][b];
            }

            for (int j = 0; j < numBanks; j++) {
                amount -= graph[b][j];
            }

            listOfNetAmounts[b].netAmount = amount;
        }

        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numBanks; i++) {
            List<Pair<Integer, String>> row = new ArrayList<>();
            for (int j = 0; j < numBanks; j++) {
                row.add(new Pair<>(0, ""));
            }
            ansGraph.add(row);
        }

        int numZeroNetAmounts = 0;
        for (int i = 0; i < numBanks; i++) {
            if (listOfNetAmounts[i].netAmount == 0) numZeroNetAmounts++;
        }

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts, numBanks);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, numBanks, minIndex, input, maxNumTypes);
            int maxIndex = maxAns.getKey();

            if (maxIndex == -1) {
                ansGraph.get(minIndex).get(0).setKey(Math.abs(listOfNetAmounts[minIndex].netAmount));
                ansGraph.get(minIndex).get(0).setValue(input[minIndex].types.iterator().next());

                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts, numBanks);
                ansGraph.get(0).get(simpleMaxIndex).setKey(Math.abs(listOfNetAmounts[minIndex].netAmount));
                ansGraph.get(0).get(simpleMaxIndex).setValue(input[simpleMaxIndex].types.iterator().next());

                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[simpleMaxIndex].netAmount == 0) numZeroNetAmounts++;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);

                ansGraph.get(minIndex).get(maxIndex).setKey(transactionAmount);
                ansGraph.get(minIndex).get(maxIndex).setValue(maxAns.getValue());

                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

                if (listOfNetAmounts[minIndex].netAmount == 0) numZeroNetAmounts++;
                if (listOfNetAmounts[maxIndex].netAmount == 0) numZeroNetAmounts++;
            }
        }

        printAns(ansGraph, numBanks, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n\t\t\t**CASH FLOW MINIMIZER**\n");

        System.out.println("Enter the number of banks:");
        int numBanks = sc.nextInt();
        sc.nextLine(); // Consume newline

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        for (int i = 0; i < numBanks; i++) {
            input[i] = new Bank();
            System.out.println("Enter bank name:");
            input[i].name = sc.nextLine();
            indexOf.put(input[i].name, i);

            System.out.println("Enter number of payment modes:");
            int numModes = sc.nextInt();
            sc.nextLine(); // Consume newline

            System.out.println("Enter payment modes (space-separated):");
            String[] modes = sc.nextLine().split(" ");
            input[i].types.addAll(Arrays.asList(modes));
        }

        System.out.println("Enter the number of transactions:");
        int numTransactions = sc.nextInt();

        int[][] graph = new int[numBanks][numBanks];
        System.out.println("Enter transactions (format: From To Amount):");
        for (int i = 0; i < numTransactions; i++) {
            String fromBank = sc.next();
            String toBank = sc.next();
            int amount = sc.nextInt();

            int fromIndex = indexOf.get(fromBank);
            int toIndex = indexOf.get(toBank);

            graph[fromIndex][toIndex] += amount;
        }

        minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, 3);
        sc.close();
    }
}
