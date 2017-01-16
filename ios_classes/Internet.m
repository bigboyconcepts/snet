//
//  Internet.m
//  ts-http
//
//  Created by Predrag Cokulov on 12/5/15.
//  Copyright © 2015 Predrag Cokulov. All rights reserved.
//

#import "Internet.h"
#import "com/tehnicomsolutions/http/Request.h"
#include "java/util/HashMap.h"
#import "com/tehnicomsolutions/http/HttpUtility.h"
#import "com/tehnicomsolutions/http/Http.h"

@implementation Internet


- (TSInternet_Response *)executeHttpRequestWithTSRequest:(TSRequest *)requestBuilder
{
    return [self executeHttpRequestWithTSRequest:requestBuilder withBoolean:true];
}

- (TSInternet_Response *)executeHttpRequestWithTSRequest:(TSRequest *)requestBuilder withBoolean:(jboolean)streamToString
{
    TSInternet_Response *response = [[TSInternet_Response alloc] init];    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL
                    URLWithString:[TSHttpUtility sanitizeUrlWithNSString:[requestBuilder getRequestUrl]]]
                                                           cachePolicy:NSURLRequestReloadIgnoringLocalAndRemoteCacheData
                                                       timeoutInterval:10
    ];

    for (NSString *key in [[requestBuilder getHeaders] keySet])
    {
        [request addValue:[[requestBuilder getHeaders] getWithId:key] forHTTPHeaderField:key];
    }


    switch ([requestBuilder getMethod].ordinal)
    {
        case TSRequest_Method_Enum_PUT:
            [request setHTTPMethod:@"PUT"];
#warning put?
            break;
        case TSRequest_Method_Enum_POST:
            [request setHTTPMethod:@"POST"];
            switch ([requestBuilder getPostMethod].ordinal)
            {
                case TSRequest_PostMethod_Enum_BODY:
                {
                    if ([requestBuilder getRequestBody] == nil)
                        @throw [NSException
                                exceptionWithName:@"NSInvalidArgumentException"
                                           reason:@"requestBody cannot be nil if post method is BODY"
                                         userInfo:nil];
                    NSData *postData = [[requestBuilder getRequestBody] dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
                    NSString *postLength = [NSString stringWithFormat:@"%lu", (unsigned long) [postData length]];
                    [request setHTTPBody:postData];
                    [request addValue:@"application/x-www-form-urlencoded charset=utf-8" forHTTPHeaderField:@"Content-Type"];
                    [request addValue:postLength forHTTPHeaderField:@"Content-Length"];
                    break;
                }
                case TSRequest_PostMethod_Enum_X_WWW_FORM_URL_ENCODED:
                {
                    NSMutableString *params = [[NSMutableString alloc] init];
                    for (NSString *key in [[requestBuilder getPOSTParams] keySet])
                    {
                        [params appendString:@"&"];
                        [params appendString:key];
                        [params appendString:@"="];
                        [params appendString:[TSHttpUtility encodeStringWithNSString:[[requestBuilder getPOSTParams] getWithId:key]]];
                    }
                    NSData *postData = [params dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
                    NSString *postLength = [NSString stringWithFormat:@"%lu", (unsigned long) [postData length]];
                    [request addValue:@"application/x-www-form-urlencoded charset=utf-8" forHTTPHeaderField:@"Content-Type"];
                    [request addValue:postLength forHTTPHeaderField:@"Content-Length"];
                    [request setHTTPBody:postData];
                    break;
                }
                case TSRequest_PostMethod_Enum_FORM_DATA:
                {
                    NSString *boundary = @"---------------------------14737809831466499882746641449";
                    NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
                    [request addValue:contentType forHTTPHeaderField:@"Content-Type"];

                    NSMutableData *body = [NSMutableData data];
                    for (NSString *key in [[requestBuilder getPOSTParams] keySet])
                    {
                        [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                        [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"\r\n\r\n%@", key, [[requestBuilder getPOSTParams] getWithId:key]] dataUsingEncoding:NSUTF8StringEncoding]];
                        [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                    }

                    for (TSRequest_UploadFile *file in [requestBuilder getFiles])
                    {
                        NSURL *fileUrl = [NSURL URLWithString:[TSHttpUtility encodeStringWithNSString:file->uri_]];
                        NSError *fileLoadingError;
                        NSData *data = [NSData dataWithContentsOfURL:fileUrl options:NSDataReadingUncached error:&fileLoadingError];
                        if (!fileLoadingError)
                        {
                            [body appendData:[[NSString stringWithFormat:@"Content-Disposition: form-data; name=\"%@\"; filename=\"%@\"\r\n", [requestBuilder getFileParamName], file->fileName_] dataUsingEncoding:NSUTF8StringEncoding]];
                            [body appendData:[@"Content-Type: application/octet-stream\r\n\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
                            [body appendData:[NSData dataWithData:data]];
                        }
                    }

                    [body appendData:[[NSString stringWithFormat:@"\r\n--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
                    [request setHTTPBody:body];
                    break;
                }
            }
            break;
        case TSRequest_Method_Enum_GET:
            [request setHTTPMethod:@"GET"];
            break;
        case TSRequest_Method_Enum_DELETE:
            [request setHTTPMethod:@"DELETE"];
            [request addValue:@"application/x-www-form-urlencoded charset=utf-8" forHTTPHeaderField:@"Content-Type"];
            break;
        default:
            @throw [NSException
                    exceptionWithName:@"NSInvalidArgumentException"
                               reason:[NSString stringWithFormat:@"Unknown Method: '%@'", [requestBuilder getMethod]]
                             userInfo:nil];
    }

    NSError *requestError = nil;
    NSHTTPURLResponse *urlResponse = nil;

    NSData *responseData = [NSURLConnection sendSynchronousRequest:request returningResponse:&urlResponse error:&requestError];
    response->responseData_ = responseData;
    response->code_ = [urlResponse statusCode];
    
    if (requestError != nil)
    {
        response->responseMessage_ = [requestError description];
    }

    if (streamToString)
    {
        response->responseDataString_ = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
    }
    if (TSHttp_LOGGING)
        NSLog(@"internetResponse = %@", response);
    return response;
}

- (NSURLRequest *)connection: (NSURLConnection *)connection
             willSendRequest: (NSURLRequest *)request
            redirectResponse: (NSURLResponse *)redirectResponse;
{
    if (redirectResponse) {
        // we don't use the new request built for us, except for the URL
        NSURL *newURL = [request URL];
        // Previously, store the original request in _originalRequest.
        // We rely on that here!
        NSMutableURLRequest *newRequest = [request mutableCopy];
        [newRequest setURL: newURL];
        return newRequest;
    } else {
        return request;
    }
}

@end
