package androidx.core.app;

public abstract class CusJobIntentService extends JobIntentService {

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (Exception i) {
            return null;
        }
    }

}
