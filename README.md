# PUPy
PUPy provides a large amount of context information through a simple interface, by taking in sensor data and condensing it into three values - privacy, unfamiliarity, and proximity, which each describe a different aspect of the context. This in an Android implementation of the theoretical framework outlined in the masters thesis of Matthew Rafuse (that's me!).

## Installation

Currently the only method through which the application can be installed on your device is via this repository. Clone the repo and build the APKs through Android Studio, and install them using `adb`.

## Development

In order to build upon PUPy, you can fork this repository and open this folder in Android Studio. From there, gradle will handle downloading and building dependencies. Open a pull request when you've completed whatever work you're aiming to do, and I'll merge in the changes!

## Disclaimer

You are using this application at your own risk. *We are not responsible for any damage caused by this application, incorrect usage or inaccuracies in this manual.*

## Publications

You can read about this work, and the theoretical framework underpinning it, in my thesis. It is currently posted [here](https://uwspace.uwaterloo.ca/handle/10012/16910).

## Credit

This work relies heavily on the work done by Hintze et al. on their authentication framework [CORMORANT](https://github.com/mobilesec/cormorant). You can read about their work in these publications:

[1] D. Hintze, R. Findling, M. Muaaz, E.Koch, R. Mayrhofer: *[CORMORANT: Towards Continuous Risk-Aware Multi-Modal Cross-Device Authentication](https://dl.acm.org/authorize?N08572)*, UbiComp/ISWC'15 Adjunct, Adjunct Proceedings of the 2015 ACM International Joint Conference on Pervasive and Ubiquitous Computing and Proceedings of the 2015 ACM International Symposium on Wearable Computers, 2015, September 13-17, Osaka, Japan, Pages 169-172

[2] D. Hintze, M. Muaaz, R. Findling, S. Scholz, E.Koch, R. Mayrhofer: *[Confidence and Risk Estimation Plugins for Multi-Modal Authentication on Mobile Devices using CORMORANT](https://dl.acm.org/citation.cfm?id=2843845)*, Proceedings of the 13th International Conference on Advances in Mobile Computing & Multimedia (MoMM 2015), December 11-13, Brussels, Belgium, Pages 384-388

[3] D. Hintze, S. Scholz, E. Koch, R. Mayrhofer: *[Location-based Risk Assesment for Mobile Authentication](https://dl.acm.org/citation.cfm?id=2971448)*, Adjunct Proceedings of the 2016 ACM International Joint Conference on Pervasive and Ubiquitous Computing and Proceedings of the 2016 ACM International Symposium on Wearable Computers, 2016, September 14-17, Heidelberg, Germany, Pages 85-88
