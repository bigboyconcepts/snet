//
// Created by Predrag Cokulov on 3/12/16.
// Copyright (c) 2016 Predrag Cokulov. All rights reserved.
//

#import "Network.h"
#import "Reachability.h"


@implementation Network
{

}
- (jboolean)isNetworkAvailable
{
    Reachability *reachability = [Reachability reachabilityForInternetConnection];
    [reachability startNotifier];

    NetworkStatus status = [reachability currentReachabilityStatus];
    [reachability stopNotifier];

    return status != NotReachable;
}

- (jboolean)isWiFiConnected
{
    Reachability *reachability = [Reachability reachabilityForInternetConnection];
    [reachability startNotifier];

    NetworkStatus status = [reachability currentReachabilityStatus];
    [reachability stopNotifier];

    return status == ReachableViaWiFi;
}


@end