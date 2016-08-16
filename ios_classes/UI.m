//
// Created by Predrag Cokulov on 3/12/16.
// Copyright (c) 2016 Predrag Cokulov. All rights reserved.
//

#import "UI.h"
#import "Toast.h"


@implementation UI
{

}
- (void)showToastWithNSString:(NSString *)message
{
    UIWindow * window = [[[UIApplication sharedApplication] delegate] window];
    [window makeToast:message];
}

- (void)showToastWithNSString:(NSString *)message withInt:(jint)length
{
    [self showToastWithNSString:message];
}

@end