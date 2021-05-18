//
//  ViewController.m
//  BDAlphaPlayerDemo
//
//  Created by ByteDance on 2020/12/21.
//

#import "ViewController.h"

#import <BDAlphaPlayer/BDAlphaPlayer.h>

@interface ViewController () <BDAlphaPlayerMetalViewDelegate>

@property (nonatomic, strong) BDAlphaPlayerMetalView *metalView;
@property (nonatomic, strong) UIButton *startBtn;
@property (nonatomic, strong) UIButton *stopBtn;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [super viewDidLoad];
    
    self.startBtn = [[UIButton alloc] initWithFrame:CGRectMake(100, 100, 60, 60)];
    self.startBtn.backgroundColor = [UIColor orangeColor];
    [self.startBtn setTitle:@"start" forState:UIControlStateNormal];
    [self.startBtn addTarget:self action:@selector(startBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.startBtn];
    
    self.stopBtn = [[UIButton alloc] initWithFrame:CGRectMake(100, CGRectGetMaxY(self.startBtn.frame) + 10, 60, 60)];
    self.stopBtn.backgroundColor = [UIColor orangeColor];
    [self.stopBtn setTitle:@"stop" forState:UIControlStateNormal];
    [self.stopBtn addTarget:self action:@selector(stopBtnClicked:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.stopBtn];
}

- (void)startBtnClicked:(UIButton *)sender
{
    if (!self.metalView) {
        self.metalView = [[BDAlphaPlayerMetalView alloc] initWithDelegate:self];
        [self.view insertSubview:self.metalView atIndex:0];
    }
    self.startBtn.hidden = YES;
    self.stopBtn.alpha = 0.3;
    
    BDAlphaPlayerMetalConfiguration *configuration = [BDAlphaPlayerMetalConfiguration defaultConfiguration];
    NSString *testResourcePath = [[[NSBundle mainBundle] bundlePath] stringByAppendingPathComponent:@"TestResource"];
    NSString *directory = [testResourcePath stringByAppendingPathComponent:@"heartbeats"];
    configuration.directory = directory;
    configuration.renderSuperViewFrame = self.view.frame;
    configuration.orientation = BDAlphaPlayerOrientationPortrait;
    
    [self.metalView playWithMetalConfiguration:configuration];
}

- (void)stopBtnClicked:(UIButton *)sender
{
    [self.metalView stopWithFinishPlayingCallback];
    [self.metalView removeFromSuperview];
    self.metalView = nil;
}

- (void)metalView:(BDAlphaPlayerMetalView *)metalView didFinishPlayingWithError:(NSError *)error
{
    if (error) {
        NSLog(@"%@", error.localizedDescription);
    }
    self.startBtn.hidden = NO;
    self.stopBtn.alpha = 1;
}

@end
