package pe.warrenth.rxjava2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.trello.rxlifecycle3.android.ActivityEvent;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BasicExample extends RxAppCompatActivity {
    private static final String TAG = "RxJava2";
    private TextView Txt;
    private Observable<String> mObservable;
    private Observer<String> mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Txt = (TextView) findViewById(R.id.txt);

        //데이터 스트림을 생성하는 옵져버블 만들기
        mObservable = Observable.just("New RxAndroid World");

        //데이터를 받아서 처리하는 옵져버 객체 만들기 -> onNext에서 처리하기
        mObserver = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(String s) {
                Txt.setText(s);

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        // [01] 옵저버블 없이 RxView 사용.
        //startExample01();

        // [02] 옵저버블 연결 RxView 사용.
        //startExample2();

        // [03] rxLifeCycle3 미적용, 적용 시 앱 화면 꺼졌을 때 log 확인.
        //startExample3();

        // [04] subscribe 에 ; 넣기
        //startExample4();

        // [05] create 수동으로 옵저버 메소드 (onNext, onError, onComplete) 를 호출 한다.
        //startExample5();

        // [06] defer()
        //startExample6();

        // [07] fromIterable()
        //startExample07();

        // [08] Thread Test
        //startExample8();

        // [08] Thread Test
    }

    private void startExample8() {
        // Scheduler.computation() : 간단한 연산이나 콜백처리에 사용. (별도의 스레드풀. 최대 CPU 갯수의 스레드풀이 순환하며 실행)
        // Scheduler.from(excutor) : 특정 executor 를 스케줄러로 사용
        // Scheduler.io() : 동기 i/o를 별도로 처리시켜 비동기 효율을 얻기위한 스케줄러. 자체 chachedThreadPool 사용. api 네트워크 호출에 유용
        // Scheduler.newThread() : 새로운 스레드 생성.
        // AndroidScheduler.mainThread() : 안드로이드 UI 스레드에서 실행..

        Observable<String> obv = Observable.defer( () -> {

            Log.i(TAG, ":"+Thread.currentThread().getName()+": defer생성시 쓰레드");
            return Observable.just("Here i am.");

        });

        obv.subscribeOn(Schedulers.computation())   // 발행자 (Observable) Thread 를 지정 - 옵져버블이 뿌려주는 쓰레드
                .observeOn(Schedulers.newThread()) // 구독자 (Observer, subscriber) Thread 를 지정 - 옵져버가 받는 곳의 쓰레드
                .subscribe(
                        s -> Log.i(TAG, ":"+Thread.currentThread().getName()+": 구독시 옵져버의 쓰레드= "+s),
                        throwable -> throwable.printStackTrace(),
                        ()-> Log.i(TAG, ":"+Thread.currentThread().getName()+": onCompleted의 쓰레드")

                );
    }

    private void startExample07() {
        ArrayList<String> datas = new ArrayList<>();
        List<String> names = new ArrayList<>();
        names.add("Cho");
        names.add("Jae");
        names.add("Seong");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(         // 인자 전달
                this,                               // 컨택스트
                android.R.layout.simple_list_item_1,// 아이템 레이아웃
                datas                               // 데이터
        );
        ((ListView) findViewById(R.id.listview)).setAdapter(adapter);

        //fromIterable 리스트 타입의 변수를 받아서, 하나씩 쪼개서 옵저버에서 전달한다.
        Observable<String> obv = Observable.fromIterable(names);
        obv.compose(bindToLifecycle())
            .subscribe(
                    s -> {datas.add(s); datas.add("+1"); }, //subscribe시 onNext자리
                    throwable -> throwable.printStackTrace(), //subscribe시 onError? 자리
                    ()->adapter.notifyDataSetChanged() //subscribe시 onComplete자리..
            );
    }

    private void startExample6() {
        // 옵저버가 구독(subscribe) 하기 전까지 옵저버블을 생성하지 않는다.
        Observable<String> obv = Observable.defer( () -> {
                    // defer 안에 () -> { retrun 옵져버블.operator } 를 통해 비동기로
                    // 옵져버블을 delay time 후에 생성해준다.
                    return Observable.just("defer");
                }
        );

        // UI 쓰레드에서.. 뭔가 비동기 작업해주면,, 메인쓰레드에서 하라고 명시해줘야한다.
        obv.delaySubscription(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe(
                    s-> ((TextView) findViewById(R.id.txt)).setText(s),
                    e-> System.out.println("dd") ,
                    ()-> Toast.makeText(this, "defer끝!", Toast.LENGTH_SHORT).show() // .create()의 onComplete에서는 불가능했던, 메소드 작업이 subscribe의 3번재 파라미터에선 가능하다
            );
    }

    private void startExample5() {
        Observable<String> obv =  Observable.create( (ObservableEmitter<String> emitter) -> {
                    emitter.onNext("onNext 1");
                    emitter.onNext("onNext 2");
                    emitter.onComplete(  );
                }
        );

        obv.compose(bindToLifecycle())
            .subscribe(s -> {
                Log.i(TAG, s);
                ((TextView) findViewById(R.id.txt)).setText(s);
            });

//        obv.compose(bindToLifecycle())
//                .subscribe(s -> {
//                    Log.i(TAG, s);
//                    ((TextView) findViewById(R.id.txt)).setText(s);
//                });
    }

    private void startExample4() {
        Observable<String> obv = Observable.just("Observable.just!", "1", "2");
        obv.compose(bindToLifecycle())
                .subscribe( s -> {
                            Log.i(TAG, s);
                            ((TextView) findViewById(R.id.txt)).setText(s);
                        }
                );
    }

    /**
     *  [03] rxLifeCycle3 미적용, 적용 시 앱 화면 꺼졌을 때 log 확인.
     */
    private void startExample3() {
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(String::valueOf)
                .subscribe(s -> Log.d(TAG, s));
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(String::valueOf)
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe(s -> Log.d(TAG, "bindUntilEvent(ActivityEvent.PAUSE) 적용한 옵저버블 " + s));
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(String::valueOf)
                .compose(bindToLifecycle())
                .subscribe(s -> Log.d(TAG, "bindToLifecycle 적용한 옵저버블 " + s));
        Observable.interval(0, 2, TimeUnit.SECONDS)
                .map(String::valueOf)
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(s -> Log.d(TAG, "bindUntilEvent(ActivityEvent.DESTROY) 적용한 옵저버블 " + s));
    }

    /**
     * [02] 옵저버블 연결 RxView 사용.
     */
    private void startExample2() {
        mObservable =  RxView.clicks(findViewById(R.id.btn))
                .map(value -> "change text");
        mObservable.subscribe(mObserver);
    }

    /**
     *  [01] 옵저버블 없이 RxView 사용.
     */
    private void startExample01() {
        RxView.clicks(findViewById(R.id.btn))
        .map(value -> "change text")
        .subscribe(value -> Txt.setText(value));
    }

    // 초간단 예제.
    public void subscribeNow(View view){
        mObservable.subscribe(mObserver);

        mObservable.just("옵져버블 변수에 = Observable.just 등으로 대입 안하고 바로 just().subscribe()하면\n 옵져버블 변수에는 데이터가  안남아있다.")
                .subscribe( s-> Toast.makeText(this, ""+s, Toast.LENGTH_SHORT).show() );

        // 최초 just로 대입한 값이 남음.
        mObservable.subscribe(s -> Log.d(TAG, "Observable의 데이터 : " + s));
    }
}
