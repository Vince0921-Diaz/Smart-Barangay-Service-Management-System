package datastructure;
import java.util.ArrayList;
import java.util.List;
public class PriorityQueue<T> {
    private static class Entry<T> { T value; int priority; long order; Entry(T value,int priority,long order){this.value=value;this.priority=priority;this.order=order;} }
    private final List<Entry<T>> items=new ArrayList<>();
    private long sequence=0;
    public void enqueue(T value,int priority){items.add(new Entry<>(value,priority,sequence++));items.sort((a,b)->{int p=Integer.compare(a.priority,b.priority);return p!=0?p:Long.compare(a.order,b.order);});}
    public T dequeue(){return items.isEmpty()?null:items.remove(0).value;}
    public T peek(){return items.isEmpty()?null:items.get(0).value;}
    public boolean isEmpty(){return items.isEmpty();}
    public int size(){return items.size();}
    public void clear(){items.clear();}
}
