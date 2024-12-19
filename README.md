# TDD


## 동시성 제어 방식에 대한 분석 및 보고서
### Java 동시성 제어 방식 synchronized VS Lock 비교
synchronized는 간단하게 동기화를 구현할 수 있는 방법입니다. 요청을 하나씩 순서대로 처리하기 때문에 여러 요청에 대한 **동시성은 보장하지만 병렬처리가 되지 않아 성능상 다른 사용자의 사용편의성을 떨어트리**는 결과를 초래할 수 있습니다.
```java
// 메서드가 선언된 객체에 대한 잠금
public synchronized UserPoint chargeUserPoints(long userId, long amount){...}
```
Lock은 수동으로 Lock의 시작점과 끝점을 설정할 수 있고 ReentrantLock의 설정으로 공정/비공정 모드가 가능한데 ReentrantLock을 true로 설정 시 FIFO 방식으로 스레드에 락을 할당하는 구조입니다. false로 설정 시 락을 요청한 스레드가 바로 락을 회득할 수 있습니다.
동시성을 제어하기 위해서는 FIFO 방식으로 스레드를 사용해야하기 때문에 true로 설정해야합니다. 또한 unlock()을 사용하여 반드시 lock을 풀어주는 작업이 강제됩니다. 
```java
  lock.lock();
        try {
            // 사용자 조회
            UserPoint userPoint = userPointTable.selectById(userId);
            if (userPoint == null) {
                throw new UserNotFoundException();
            }
            UserPoint updatedPoint = operation.apply(userPoint);
            return userPointTable.insertOrUpdate(updatedPoint.id(), updatedPoint.point());

        }finally {
            lock.unlock();
        }
```
ConcurrentHashMap에서 생성된 락(ReentrantLock)을 재사용하지 않고 무조건 생성하면 메모리가 증가할 수 있고 잘못된 Id값으로 생성되는 Lock을 방지하기 위해 Lock 생성 전 예외 처리를 하는 것이 메모리 관리에 중요합니다.
```java
  if (userId < 0) throw new InvalidUserIdException();
  final Lock lock = locks.computeIfAbsent(userId, id -> new ReentrantLock(true));
```

### 동시성 방식 적용
동시성을 제어하기 위해 ConcurrentHashMap과 ReentrantLock을 결합하여 사용했습니다. 
ConcurrentHashMap은 데이터를 여러 개의 버킷으로 나누어 관리하며 각 버킷에 대한 락을 독립적으로 처리하여 여러 스레드가 동시에 맵에 접근할 수 있습니다.
이런 특징이 멀티스레드 환경에서도 높은 성능과 안정성을 제공합니다.


![image](https://github.com/user-attachments/assets/d696096c-5cf7-4e46-8338-eb0bee567f4b)

### 고려했지만 적용하지 않은 기술
#### BlockingQueue
BlockingQueue를 사용하지 않은 이유는 사용자 단위로 작업을 분리하면 같은 사용자의 순서보장 및 서로 다른 사용자 요청 간 병렬 처리 까지 한번에 순서를 보장할 수 있습니다.
이런 동시성을 보장하지만 메모리 사용량과 사용자 마다 스레드를 각각 사용해야하는 제한사항이 대용량 서비스로 갈 수록 사용하기 힘든 문제로 사용하지 않게 되었다.
