public class VoiceResponse
{
    private String RecognitionStatus;

    private String DisplayText;

    private String Duration;

    private String Offset;

    public String getRecognitionStatus ()
    {
        return RecognitionStatus;
    }

    public void setRecognitionStatus (String RecognitionStatus)
    {
        this.RecognitionStatus = RecognitionStatus;
    }

    public String getDisplayText ()
    {
        return DisplayText;
    }

    public void setDisplayText (String DisplayText)
    {
        this.DisplayText = DisplayText;
    }

    public String getDuration ()
    {
        return Duration;
    }

    public void setDuration (String Duration)
    {
        this.Duration = Duration;
    }

    public String getOffset ()
    {
        return Offset;
    }

    public void setOffset (String Offset)
    {
        this.Offset = Offset;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [RecognitionStatus = "+RecognitionStatus+", DisplayText = "+DisplayText+", Duration = "+Duration+", Offset = "+Offset+"]";
    }
}