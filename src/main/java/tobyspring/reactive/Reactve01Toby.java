package tobyspring.reactive;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Reactive 변경사항이 생길경우 그것에 대한 대응 방식?
@SuppressWarnings("unused")
public class Reactve01Toby {
	//	public static void main(String[] args) {
	//		
	////		1. Iterator 구현
	//		// Duality (상대성)
	//		// 옵저버 패턴
	//		// Reactive Streams (표준) 
	//		//	List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
	//		Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);
	//		// Iterable을 이용해 만든 클래스는 for each 를 사용할 수 있다.
	//		/* Implementing this interface allows an object to be the target of
	//		* the "for-each loop" statement. See
	//		*/
	//		// iterator 함수 : 실제로 이터레이터를 이용할 때 사용하는 메소드
	//		for (Integer i : iter) {
	//			System.out.println(i);
	//		}
	//
	//		Iterable<Integer> customIterbleClass = () -> new Iterator<Integer>() {
	//			int i = 0;
	//			final static int MAX = 10;
	//			@Override
	//			public boolean hasNext() {
	//				return i < MAX;
	//			}
	//
	//			@Override
	//			public Integer next() {
	//				return ++i;
	//			}
	//		};
	//		
	//		for(Integer i: customIterbleClass)	{
	//			System.out.println(i);
	//		}
	////		for(Iterator<Integer> it = iter.iterator(); it.hasNext();)	{
	////			System.out.println(it.next());
	////		}
	//		
	//
	//	}
	
	/* 
	 * 문제제기
	 * 1. Complete???
	 * 2. error
	 */
	
	// Data Source
	static class IntObservable extends Observable implements Runnable	{

		public void run() {
			for(int i = 1; i <= 10; i++) {
				setChanged();
				notifyObservers(i); // push	
				// int i = it.next(); 	// pull
			}
		}

	}

	public static void main(String[] args) {
		// Source -> Event/Data -> Observer
		// Subscriber
		Observer ob = new Observer() {

			public void update(Observable o, Object arg) {
				System.out.println(Thread.currentThread().getName() + "-" + arg);
			}
		};

		IntObservable io = new IntObservable();
		io.addObserver(ob);

		ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(io);
		System.out.println(Thread.currentThread().getName() + " EXIT ");
		es.shutdown();

	}

}
