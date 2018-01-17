package utility;

public class QueuePair<A extends Comparable<A>, B> implements Comparable<QueuePair<A, B>>
{
    private final A first;
    private final B second;

    public QueuePair(A first, B second)
    {
        this.first = first;
        this.second = second;
    }

    public A getFirst()
    {
        return first;
    }

    public B getSecond()
    {
        return second;
    }

    @Override
    public boolean equals(Object oth)
    {
        if (this == oth)
        {
            return true;
        }
        if (oth == null || !(getClass().isInstance(oth)))
        {
            return false;
        }
        QueuePair<A, B> other = getClass().cast(oth);
        return ((first == null) ? (other.first == null) : (first.equals(other.first)) &&
                (second == null ? other.second == null : second.equals(other.second)));
    }

    @Override
    public int compareTo(QueuePair<A, B> o)
    {
        return o.first.compareTo(this.first);
    }
}