//
//  SimpleRequestHandler.m
//  ts-http
//
//  Created by Predrag Cokulov on 12/5/15.
//  Copyright © 2015 Predrag Cokulov. All rights reserved.
//

#import "SimpleRequestHandler.h"
#import "com/tehnicomsolutions/http/ResponseParser.h"
#import "com/tehnicomsolutions/http/Internet.h"
#import "com/tehnicomsolutions/http/Request.h"
#import "com/tehnicomsolutions/http/Http.h"
#import "MBProgressHUD.h"

@implementation SimpleRequestHandler

-(TSResponseParser*)handleRequestWithRequestCode: (int)requestCode andRequestBuilder: (TSRequest*)requestBuilder sync: (BOOL)sync;
{
    TSInternet_Response *response = [[TSHttp getInstance] ->internet_ executeHttpRequestWithTSRequest:requestBuilder];
    return [[TSResponseParser alloc] initWithTSInternet_Response:response];
}

-(void)handlePreRequestWithRequestCode: (int)reqestCode sync: (BOOL)sync
{
    UIWindow *keyWindow = [[[UIApplication sharedApplication] delegate] window];
    [MBProgressHUD showHUDAddedTo:keyWindow animated:YES];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = TRUE;
    
}

-(void)handlePostRequestWithRequestCode: (int)requestCode andRequestBuilder: (TSRequest*)requestBuilder andResponseParser: (TSResponseParser*) responseParser sync: (BOOL)sync
{
    UIWindow *keyWindow = [[[UIApplication sharedApplication] delegate] window];
    [MBProgressHUD hideHUDForView:keyWindow animated:YES];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = false;
}

- (void)handleRequestCanceledWithRequestCode:(jint)requestCode andResponseParser:(TSResponseParser *)parser sync:(jboolean)sync
{
    UIWindow *keyWindow = [[[UIApplication sharedApplication] delegate] window];
    [MBProgressHUD hideHUDForView:keyWindow animated:YES];
    [UIApplication sharedApplication].networkActivityIndicatorVisible = false;
}

@end
