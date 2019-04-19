package bhestie.levpos.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import bhestie.levpos.Pawn;

public final class HistoryStorage implements Collection<String>, Cloneable{
	private Set<String> storage;
	public HistoryStorage(){
		this.storage = new HashSet<String>();
	}
	private HistoryStorage(Set<String> base){
		this.storage = new HashSet<String>(base);
	}
	
	@Override
	public HistoryStorage clone(){
		return new HistoryStorage(this.storage);
	}
	@Override
	public int size() {
		return this.storage.size();
	}
	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}
	@Override
	@Deprecated
	public boolean contains(Object o) {
		return this.storage.contains(o);
	}
	/**
	 * Check if the list is present
	 * EXECUTES INTERNAL CONVERSIONS
	 * @param input the list to test
	 * @return if the converted list is present
	 */
	public boolean includes(List<Pawn> input){
		return this.contains(this.convert(input));
	}
	@Override
	public Iterator<String> iterator() {
		return this.storage.iterator();
	}
	@Override
	public Object[] toArray() {
		return this.storage.toArray();
	}
	@Override
	public <T> T[] toArray(T[] a) {
		return this.storage.toArray(a);
	}
	@Override
	@Deprecated
	public boolean add(String e) {
		return this.storage.add(e);
	}
	private String convert(List<Pawn> input){
		input.sort(HistoryStorage.pawnparator);
		return input.toString();
	}
	/**
	 * Add the list to the set if it's not present
	 * @param l the list to add
	 * @return if the insert was successful
	 * @throws IllegalArgumentException if the list was present
	 */
	public boolean add(List<Pawn> l) throws IllegalArgumentException{
		if(this.includes(l)){
			throw new IllegalArgumentException();
		}
		return this.add(this.convert(l));
	}
	@Override
	public boolean remove(Object o) {
		return this.storage.remove(o);
	}
	@Override
	@Deprecated
	public boolean containsAll(Collection<?> c) {
		return this.storage.containsAll(c);
	}
	@Override
	public boolean addAll(Collection<? extends String> c) {
		return this.storage.addAll(c);
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		return this.storage.removeAll(c);
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return this.storage.retainAll(c);
	}
	@Override
	public void clear() {
		this.storage.clear();
	}
	
	private static final Comparator<Pawn> pawnparator = new Comparator<Pawn>(){
		@Override
		public int compare(Pawn p1, Pawn p2) {
			final int dx = p1.getPosition().x - p2.getPosition().x;
			if(dx != 0){
				return dx;
			}else{
				return p1.getPosition().y - p2.getPosition().y;
			}
		}
	};
	private static HistoryStorage instance = null;
	/**
	 * Get a new {@link HistoryStorage} object used as Singleton pattern statically for all the code.
	 * NOT MULTITHREADED
	 * @return the {@link HistoryStorage} object.
	 */
	public static HistoryStorage get(){
		if(instance ==  null){
			instance = new HistoryStorage();
		}
		return instance;
	}
}
