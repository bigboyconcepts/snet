//
//  NoInternetConnectionHandler.m
//  ts-http
//
//  Created by Predrag Cokulov on 3/12/16.
//  Copyright © 2016 Predrag Cokulov. All rights reserved.
//

#import "NoInternetConnectionHandler.h"
#import "AppDelegate.h"
#import <UIKit/UIKit.h>

@interface NoInternetConnectionHandler()

@property BOOL alertShowing;

@end

@implementation NoInternetConnectionHandler

- (void)handleNoInternetConnection
{
    if(_alertShowing)
        return;
    
    UIAlertController * alert=   [UIAlertController
                                  alertControllerWithTitle:@"No Connection"
                                  message:@"An internet connection is required to continue.\n\nPlease check if your device is connected and try again"
                                  preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction* okButton = [UIAlertAction
                                actionWithTitle:@"OK"
                                style:UIAlertActionStyleDefault
                                handler:^(UIAlertAction * action)
                                {
                                    _alertShowing = false;
                                    
                                }];
    
    [alert addAction:okButton];
    
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:alert animated:YES completion:nil];

}



@end
