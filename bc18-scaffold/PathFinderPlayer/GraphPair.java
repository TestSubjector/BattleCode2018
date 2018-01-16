public class GraphPair<A, B extends Comparable<B>> implements Comparable<GraphPair<A, B>>
{
    private final A first;
    private final B second;

    public GraphPair(A first, B second)
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
        GraphPair<A, B> other = getClass().cast(oth);
        return ((first == null) ? (other.first == null) : (first.equals(other.first)) &&
                (second == null ? other.second == null : second.equals(other.second)));
    }

    @Override
    public int compareTo(GraphPair<A, B> o)
    {
        return this.second.compareTo(o.second);
    }
}