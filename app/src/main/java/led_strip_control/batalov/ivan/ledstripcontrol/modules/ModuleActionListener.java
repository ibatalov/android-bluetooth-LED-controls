package led_strip_control.batalov.ivan.ledstripcontrol.modules;

import android.os.Bundle;

/**
 * Created by ivan on 5/17/16.
 */
public interface ModuleActionListener {
    void sendMessage(String message, Bundle options);
    int getLastSentMode();
    void onModuleSelected(LedModule module);
}
