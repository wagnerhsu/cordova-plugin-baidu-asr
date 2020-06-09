//
//  CDVBaiduASR.m
//  myTestCordova
//
//  Created by hankers.yan on 2020/5/11.
//

#import <Cordova/CDV.h>
#import "CDVBaiduASR.h"
#import "BDSEventManager.h"
#import "BDSASRDefines.h"
#import "BDSASRParameters.h"
#import "BDTheme.h"
#import "BDRecognizerViewParamsObject.h"
#import "BDRecognizerViewController.h"
#import "BDRecognizerViewDelegate.h"

// 默认的识别成功的回调
void (^_successHandler)(id);
// 默认的识别失败的回调
void (^_failHandler)(NSError *);

@interface CDVBaiduASR () <BDSClientASRDelegate, BDRecognizerViewDelegate>

@property(strong, nonatomic) BDSEventManager *asrEventManager;
@property(nonatomic, strong) BDRecognizerViewController *recognizerViewController;

@property(nonatomic, assign) BOOL longSpeechFlag;

@end

@implementation CDVBaiduASR {
}

NSString* APP_ID = nil;
NSString* API_KEY = nil;
NSString* SECRET_KEY = nil;
NSString* _callbackId = nil;

- (void)pluginInitialize {
    // Key
    NSDictionary* dic = [self.commandDelegate settings];
    APP_ID = [dic valueForKey:@"app_id"];
    API_KEY = [dic valueForKey:@"api_key"];
    SECRET_KEY = [dic valueForKey:@"secret_key"];

    self.asrEventManager = [BDSEventManager createEventManagerWithName:BDS_ASR_NAME];
    //[[BDVRSettings getInstance] configBDVRClient];
    [self configVoiceRecognitionClient];
}

- (void)startOnline:(CDVInvokedUrlCommand *)command {
    _callbackId = command.callbackId;
    [self.asrEventManager setParameter:@"" forKey:BDS_ASR_AUDIO_FILE_PATH];
    //[self configFileHandler];
    [self configRecognizerViewController];
    [self.recognizerViewController startVoiceRecognition];
}

- (void)startLong:(CDVInvokedUrlCommand *)command {
    _callbackId = command.callbackId;
    [self longSpeechRecognition];
}

- (void)stopLong:(CDVInvokedUrlCommand *)command {
    self.longSpeechFlag = NO;
    [self.asrEventManager sendCommand:BDS_ASR_CMD_STOP];
}

- (void)configVoiceRecognitionClient {
    //设置DEBUG_LOG的级别
    [self.asrEventManager setParameter:@(EVRDebugLogLevelTrace) forKey:BDS_ASR_DEBUG_LOG_LEVEL];
    //配置API_KEY 和 SECRET_KEY 和 APP_ID
    [self.asrEventManager setParameter:@[API_KEY, SECRET_KEY] forKey:BDS_ASR_API_SECRET_KEYS];
    [self.asrEventManager setParameter:APP_ID forKey:BDS_ASR_OFFLINE_APP_CODE];
    [self.asrEventManager setParameter:@"1537" forKey:BDS_ASR_PRODUCT_ID];
    
    //配置端点检测（二选一）
    [self configModelVAD];
    //    [self configDNNMFE];
    
    //    [self.asrEventManager setParameter:@"15361" forKey:BDS_ASR_PRODUCT_ID];
    // ---- 语义与标点 -----
    //    [self enableNLU];
    //    [self enablePunctuation];
    // ------------------------
    
    //---- 语音自训练平台 ----
//        [self configSmartAsr];
}

- (void)configModelVAD {
    NSString *modelVAD_filepath = [[NSBundle mainBundle] pathForResource:@"bds_easr_basic_model" ofType:@"dat"];
    [self.asrEventManager setParameter:modelVAD_filepath forKey:BDS_ASR_MODEL_VAD_DAT_FILE];
    [self.asrEventManager setParameter:@(YES) forKey:BDS_ASR_ENABLE_MODEL_VAD];
}

- (void)configFileHandler {
//    self.fileHandler = [self createFileHandleWithName:@"recoder.pcm" isAppend:NO];
}

- (void)configRecognizerViewController {
    BDRecognizerViewParamsObject *paramsObject = [[BDRecognizerViewParamsObject alloc] init];
    paramsObject.isShowTipAfterSilence = YES;
    paramsObject.isShowHelpButtonWhenSilence = NO;
    paramsObject.tipsTitle = @"您可以这样问";
    paramsObject.tipsList = [NSArray arrayWithObjects:@"我要吃饭", @"我要买电影票", @"我要订酒店", nil];
    paramsObject.waitTime2ShowTip = 0.5;
    paramsObject.isHidePleaseSpeakSection = YES;
    paramsObject.disableCarousel = YES;
    self.recognizerViewController = [[BDRecognizerViewController alloc] initRecognizerViewControllerWithOrigin:CGPointMake(9, 80)
                                                                                                         theme:nil
                                                                                              enableFullScreen:YES
                                                                                                  paramsObject:paramsObject
                                                                                                      delegate:self];
}

- (void)longSpeechRecognition
{
    self.longSpeechFlag = YES;
    [self.asrEventManager setParameter:@(NO) forKey:BDS_ASR_NEED_CACHE_AUDIO];
    [self.asrEventManager setParameter:@"" forKey:BDS_ASR_OFFLINE_ENGINE_TRIGGERED_WAKEUP_WORD];
    [self.asrEventManager setParameter:@(YES) forKey:BDS_ASR_ENABLE_LONG_SPEECH];
    // 长语音请务必开启本地VAD
    [self.asrEventManager setParameter:@(YES) forKey:BDS_ASR_ENABLE_LOCAL_VAD];
    [self voiceRecogButtonHelper];
}

- (void)voiceRecogButtonHelper
{
    // [self configFileHandler];
    [self.asrEventManager setDelegate:self];
    [self.asrEventManager setParameter:nil forKey:BDS_ASR_AUDIO_FILE_PATH];
    [self.asrEventManager setParameter:nil forKey:BDS_ASR_AUDIO_INPUT_STREAM];
    [self.asrEventManager sendCommand:BDS_ASR_CMD_START];
}

#pragma mark - MVoiceRecognitionClientDelegate

- (void)VoiceRecognitionClientWorkStatus:(int)workStatus obj:(id)aObj {
    switch (workStatus) {
        case EVoiceRecognitionClientWorkStatusFinish: {
            NSString* result = [NSString stringWithFormat:@"CALLBACK: final result - %@.\n\n", [self getDescriptionForDic:aObj]];
            [self printLogTextView:result];
            if (aObj) {
                [self onResult:aObj];
            }
//            if (!self.longSpeechFlag) {
//                [self onEnd];
//            }
            break;
        }
        default:
            break;
    }
}

-(void) onResult:(NSDictionary*)resDic {
    NSString* result = [[resDic valueForKey:@"results_recognition"] componentsJoinedByString:@","];
    
    NSMutableDictionary* resultDic = [NSMutableDictionary dictionary];
    [resultDic setObject:result forKey:@"result"];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDic];
    [pluginResult setKeepCallback:[NSNumber numberWithInt:1]];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
}

- (void)onEndWithViews:(BDRecognizerViewController *)aBDRecognizerViewController withResult:(id)aResult
{
    if (aResult) {
        [self onResult:aResult];
    }
    [self.asrEventManager setDelegate:self];
}

- (NSString *)getDescriptionForDic:(NSDictionary *)dic {
    if (dic) {
        return [[NSString alloc] initWithData:[NSJSONSerialization dataWithJSONObject:dic
                                                                              options:NSJSONWritingPrettyPrinted
                                                                                error:nil] encoding:NSUTF8StringEncoding];
    }
    return nil;
}

- (void)printLogTextView:(NSString *)logString
{
//    self.logTextView.text = [logString stringByAppendingString:_logTextView.text];
//    [self.logTextView scrollRangeToVisible:NSMakeRange(0, 0)];
    NSLog(@"%@", logString);
}

@end
