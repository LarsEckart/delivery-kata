package kata;

import org.lambda.functions.Function1;

import java.util.List;

public record IndexSection<T>(T previous, T current, T next) {

    public static <T> IndexSection<T> getIndexSection(List<T> list, Function1<T, Boolean> predicate) {
        int index = getIndexOfDelivery(list, predicate);

        T current = list.get(index);
        T previous = 0 < index ? list.get(index - 1) : null;
        T next = index < list.size() - 1 ? list.get(index + 1) : null;
        return new IndexSection<>(previous, current, next);
    }

    private static <T> int getIndexOfDelivery(List<T> list, Function1<T, Boolean> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.call(list.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
