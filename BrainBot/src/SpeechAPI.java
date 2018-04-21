public class SpeechAPI {

  /** 
   * For mode information on the recognition modes, please refer to 
   * <a href="https://docs.microsoft.com/en-us/azure/cognitive-services/speech/api-reference-rest/bingvoicerecognition#recognition-modes"/>
   */
  public static enum RecognitionMode { Interactive, Conversation, Dictation }

  /** 
   * For mode information on the output format, please refer to 
   * <a href="https://docs.microsoft.com/en-us/azure/cognitive-services/speech/api-reference-rest/bingvoicerecognition#output-format"/>
   */
  public static enum OutputFormat { Simple, Detailed }

  /** 
   * For mode information on the supported languages, please refer to 
   * <a href="https://docs.microsoft.com/en-us/azure/cognitive-services/speech/api-reference-rest/bingvoicerecognition#recognition-language"/>
   */
  public static enum Language { ar_EG, ca_ES, da_DK, de_DE, en_AU, en_CA, en_GB, en_IN, en_NZ, en_US, 
                                es_ES, es_MX, fi_FI, fr_CA, fr_FR, hi_IN, it_IT, ja_JP, ko_KR, nb_NO, 
                                nl_NL, pl_PL, pt_BR, pt_PT, ru_RU, sv_SE, zh_CN, zh_HK, zh_TW  }
}