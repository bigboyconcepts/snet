//
// Created by Predrag Cokulov on 3/12/16.
// Copyright (c) 2016 Predrag Cokulov. All rights reserved.
//

#import <com/tehnicomsolutions/http/Http.h>
#import "ts_http.h"
#import "Network.h"
#import "Internet.h"
#import "UI.h"
#import "TextManager.h"


@implementation ts_http
{

}
+ (void)bootstrap
{
    Network *network = [[Network alloc] init];
    Internet *internet = [[Internet alloc] init];
    UI *ui = [[UI alloc] init];
    TextManager *textManager = [[TextManager alloc] init];

	[TSHttp initialize__WithTSNetwork:network withTSInternet:internet withTSUI:ui withTSTextManager:textManager];
}

@end