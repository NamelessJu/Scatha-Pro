package namelessju.scathapro.alerts.alertmodes.customalertmode;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import namelessju.scathapro.ScathaPro;

public interface ICustomAlertModeSaveable
{
    public void saveChanges(SaveResults results);
    
    public static class SaveResults
    {
        public final String customModeId;
        private boolean resourceReloadRequested = false;
        private boolean propertiesSavingRequired = false;
        private List<AudioConversion> audioConversions = Lists.newArrayList();
        
        public SaveResults(String customModeId)
        {
            this.customModeId = customModeId;
        }
        
        public void requestResourceReload()
        {
            resourceReloadRequested = true;
        }
        
        public boolean isResourceReloadRequired()
        {
            return (resourceReloadRequested || hasAudioConversions()) && ScathaPro.getInstance().getCustomAlertModeManager().isSubmodeActive(customModeId);
        }
        
        public void requestPropertiesSave()
        {
            propertiesSavingRequired = true;
        }
        
        public boolean isPropertiesSavingRequired()
        {
            return propertiesSavingRequired;
        }
        
        public void addAudioConversion(AudioConversion conversion)
        {
            if (audioConversions.contains(conversion)) return;
            audioConversions.add(conversion);
        }
        
        public boolean hasAudioConversions()
        {
            return audioConversions.size() > 0;
        }
        
        public AudioConversion[] getAudioConversions()
        {
            if (!hasAudioConversions()) return new AudioConversion[0];
            return audioConversions.toArray(new AudioConversion[0]);
        }
        
        public static class AudioConversion
        {
            public File from;
            public File to;
            
            public AudioConversion(File from, File to)
            {
                this.from = from;
                this.to = to;
            }
            
            public boolean isValid()
            {
                return from != null && to != null && from.exists();
            }
        }
    }
}
