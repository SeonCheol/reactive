### Reactive 란?

이벤트 방식의 프로그래밍?

Reactive?

Functional Reactive?

> Reactive 외부 이벤트나 데이터 변경 같이 어떠한 사건이 발생하면 그 것에 맞추어 대응하는 프로그래밍 방식.

**Duality** 
리액티브에 대한 아이디어를 얘기할 때 항상 나오는 개념. 수학적 개념으로 구조를 뒤집어서 구성하는 것을 말한다. 한국어로는 ''쌍대성''

**Observer Pattern**
listener와 event를 이용한 event drive 방식의 패턴

**Reactive Streams**
JVM 기술을 개발하는 회사들이 만든 표준 -> Java9 API에 포함.

### Iterable 상속

```java
List<Integer> list = Arrays.asList(1, 2, 3);
for(Integer i : list)	{ // for-each
    System.out.println(i);
}
```

위의 코드는 List 라서 for-each 구문을 사용할 수 있는 게 아니라 List가 Iterable을 구현하고 있기 때문에 for-each 구문을 사용할 수 있다. 

이는 다시 말해, Collection이 아니더라도 Iterable을 상속하면 for-each 를 사용할 수 있다는 것이다.

##### 커스텀 Iterable 구현

```java
public interface Iterable<T> {
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    Iterator<T> iterator(); // 순수하게 구현해야 할 메소드
    ....
```

Iterable 인터페이스에는 forEach(), spliterator(), iterator() 세 가지의 메소드가 있지만 앞의 두 메소드는 default로 구현되어 있어서 Iterable을 사용하기 위해 순수하게 구현해야할 메소드는 iterator() 하나 뿐이다.

```java
Iterable<Integer> iter = () -> new Iterator<Integer> () {
    int i = 0;
    final static int MAX = 10;
    @Override
	public boolean hasNext() {	// 다음 원소 반환 가능한 지 여부
		return i < MAX;
	}
	@Override
	public Integer next() { // 다음 원소 반환 
		return ++i;
	}
};

for(Integer i : iter)	{
    System.out.println(i);
}
```

