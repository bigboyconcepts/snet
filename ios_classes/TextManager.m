//
// Created by Predrag Cokulov on 3/12/16.
// Copyright (c) 2016 Predrag Cokulov. All rights reserved.
//

#import "TextManager.h"


@implementation TextManager
{

}
- (NSString *)getTextWithNSString:(NSString *)key
{
    return NSLocalizedString(key, nil);
}

@end