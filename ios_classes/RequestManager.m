//
// Created by Predrag Cokulov on 3/12/16.
// Copyright (c) 2016 Predrag Cokulov. All rights reserved.
//

#import "RequestManager.h"
#import "java/lang/Runnable.h"

@implementation RequestManager
{

}

- (void)runOnUIThread:(id <JavaLangRunnable>)runnable
{
    dispatch_async(dispatch_get_main_queue(), ^(void)
    {
        [runnable run];
    });
}


@end