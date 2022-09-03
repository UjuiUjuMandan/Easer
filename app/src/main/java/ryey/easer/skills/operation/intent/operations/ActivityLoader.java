package ryey.easer.skills.operation.intent.operations;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import ryey.easer.skills.operation.intent.IntentLoader;
import ryey.easer.skills.operation.intent.IntentOperationData;

public class ActivityLoader extends IntentLoader<IntentOperationData> {

    public ActivityLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    public void _load(@NonNull IntentOperationData data, @NonNull OnResultCallback callback) {
        Intent intent = getIntent(data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        callback.onResult(true);
    }
}
