import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CollectionsStreamsGenerics {
    static final class StudentRecord implements Comparable<StudentRecord> {
        private final String name;
        private final int score;

        StudentRecord(String name, int score) {
            this.name = name;
            this.score = score;
        }

        String getName() {
            return name;
        }

        int getScore() {
            return score;
        }

        @Override
        public int compareTo(StudentRecord other) {
            return name.compareTo(other.name);
        }

        @Override
        public String toString() {
            return name + "=" + score;
        }
    }

    static final class Box<T> {
        private final T value;

        Box(T value) {
            this.value = value;
        }

        T getValue() {
            return value;
        }
    }

    private static <T extends Comparable<? super T>> T maxOf(T first, T second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    private static double sumNumbers(List<? extends Number> values) {
        double total = 0;
        for (Number value : values) {
            total += value.doubleValue();
        }
        return total;
    }

    private static void addDefaultScores(List<? super Integer> scores) {
        scores.add(0);
        scores.add(100);
    }

    public static void main(String[] args) {
        List<StudentRecord> records = new ArrayList<>(List.of(
            new StudentRecord("Mina", 91),
            new StudentRecord("Ava", 87),
            new StudentRecord("Noah", 74),
            new StudentRecord("Ava", 95)
        ));

        LinkedList<String> linkedList = new LinkedList<>(List.of("first", "second"));
        linkedList.addFirst("start");
        System.out.println("LinkedList: " + linkedList);

        Set<String> hashSet = new HashSet<>(List.of("pear", "apple", "pear"));
        Set<String> insertionOrder = new LinkedHashSet<>(List.of("pear", "apple", "pear"));
        Set<String> sortedSet = new TreeSet<>(List.of("pear", "apple", "pear"));
        System.out.println("HashSet: " + hashSet);
        System.out.println("LinkedHashSet: " + insertionOrder);
        System.out.println("TreeSet: " + sortedSet);

        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Ava", 87);
        Map<String, Integer> insertionMap = new LinkedHashMap<>();
        insertionMap.put("Mina", 91);
        insertionMap.put("Ava", 87);
        Map<String, Integer> sortedMap = new TreeMap<>(insertionMap);
        System.out.println("HashMap: " + hashMap);
        System.out.println("LinkedHashMap: " + insertionMap);
        System.out.println("TreeMap: " + sortedMap);

        Queue<String> queue = new LinkedList<>();
        queue.offer("first job");
        queue.offer("next job");
        System.out.println("Queue head: " + queue.poll());

        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("front");
        deque.addLast("back");
        System.out.println("Deque: " + deque);

        PriorityQueue<Integer> priorities = new PriorityQueue<>();
        priorities.offer(4);
        priorities.offer(1);
        priorities.offer(3);
        System.out.println("Next priority: " + priorities.poll());

        List<String> iteratorNames = new ArrayList<>(List.of("keep", "remove"));
        Iterator<String> iterator = iteratorNames.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals("remove")) {
                iterator.remove();
            }
        }
        System.out.println("Iterator removal: " + iteratorNames);

        List<String> editableNames = new ArrayList<>(List.of("Ava", "Mina"));
        ListIterator<String> listIterator = editableNames.listIterator();
        while (listIterator.hasNext()) {
            listIterator.set(listIterator.next().toUpperCase());
        }
        System.out.println("ListIterator update: " + editableNames);

        Collections.sort(records);
        System.out.println("Comparable order: " + records);
        records.sort(Comparator.comparingInt(StudentRecord::getScore).reversed());
        System.out.println("Comparator order: " + records);
        System.out.println("Highest score: " + Collections.max(records, Comparator.comparingInt(StudentRecord::getScore)));
        System.out.println("Ava record count: " + Collections.frequency(
            records.stream().map(StudentRecord::getName).toList(), "Ava"
        ));

        List<String> names = records.stream()
            .filter(record -> record.getScore() >= 80)
            .map(StudentRecord::getName)
            .sorted()
            .distinct()
            .skip(1)
            .limit(2)
            .toList();
        System.out.println("Stream results: " + names);

        Box<String> label = new Box<>("Java generics");
        List<Number> numbers = new ArrayList<>(List.of(2, 3.5));
        List<Integer> defaults = new ArrayList<>();
        addDefaultScores(defaults);
        System.out.println(label.getValue());
        System.out.println("Maximum: " + maxOf(12, 19));
        System.out.println("Sum: " + sumNumbers(numbers));
        System.out.println("Wildcard consumer: " + defaults);
    }
}
