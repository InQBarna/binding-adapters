package com.inqbarna.libsamples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.inqbarna.adapters.ItemBinder;
import com.inqbarna.adapters.RxPaginatedBindingAdapter;
import com.inqbarna.adapters.TypeMarker;
import com.inqbarna.adapters.VariableBinding;
import com.inqbarna.common.paging.PaginatedAdapterDelegate;
import com.inqbarna.rxutil.paging.PageFactory;
import com.inqbarna.rxutil.paging.RxPagingCallback;
import com.inqbarna.rxutil.paging.RxPagingConfig;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.inqbarna.libsamples.Root.borrame;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.list)
    RecyclerView list;

    @BindView(R.id.progress)
    View progress;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private RxPagingCallback mPagingCallback = new RxPagingCallback() {
        @Override
        public void onError(Throwable throwable) {
            Toast.makeText(MainActivity.this, "Error detectado", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCompleted() {

        }
    };


    private PaginatedAdapterDelegate.ProgressHintListener mProgressListener = new PaginatedAdapterDelegate.ProgressHintListener() {
        @Override
        public void setLoadingState(boolean loading) {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    };

    private ItemBinder mItemBinder = new ItemBinder() {

        @Override
        public void bindVariables(VariableBinding variableBinding, int pos, TypeMarker dataAtPos) {
            variableBinding.bindValue(com.inqbarna.libsamples.BR.model, dataAtPos);
        }
    };
    private RxPaginatedBindingAdapter<TestVM> mAdapter;

    public static class TestVM implements TypeMarker {
        public final String value;

        public TestVM(int idx) {
            value = "Cell number: " + idx;
        }

        @Override
        public int getItemType() {
            return R.layout.main_test_item;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);




        mAdapter = new RxPaginatedBindingAdapter<>(mPagingCallback, new RxPagingConfig.Builder().build(), mProgressListener);
        mAdapter.setItemBinder(mItemBinder);
        list.setAdapter(mAdapter);

        mAdapter.setDataFactory(createFactory(), 20);
    }

    private PageFactory<TestVM> createFactory() {
        return new PageFactory<TestVM>() {
            @Override
            public Observable<? extends TestVM> nextPageObservable(int start, int size) {
                int endElem = Math.min(start + size, 1150);
                size = endElem - start;
                return Observable.range(start, size).subscribeOn(Schedulers.io()).delaySubscription(10, TimeUnit.SECONDS)
                        .map(
                                new Func1<Integer, TestVM>() {
                                    @Override
                                    public TestVM call(Integer integer) {
                                        return new TestVM(integer);
                                    }
                                }
                        );
            }
        };
    }
}
