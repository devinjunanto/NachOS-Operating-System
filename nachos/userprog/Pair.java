package nachos.userprog;

public class Pair<K, V> {
    private final K a;
    private final V b;

    public static <K, V> Pair<K, V> createPair(K a, V b) {
        return new Pair<K, V>(a, b);
    }

    public Pair(K A, V B) {
        this.a = A;
        this.b = B;
    }

    public K getKey() {
        return a;
    }

    public V getValue() {
        return b;
    }
}
