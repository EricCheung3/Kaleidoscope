# Kaleidoscope README #
In this project, we develop  a platform for multimedia streaming on mobile devices, enhanced with textual and touch-display interactions for a rich user experience. Users (senders) can use our Kaleidoscope mobile application to setup a streaming channel on the platform server, and invite their contacts (receivers) to share their real-time video recordings. At the Sender site, the Kaleidoscope app captures the video and shares it with the streaming server. At the Receiver site, the Kaleidoscope app replays the video. At both sites, users can send text messages to the connected peers and touch the display to point out interesting video scenes; the Kaleidoscope app shares these interactions with the whole set of peers. The data (audio/video, text, touch events) will be stored on the cloud server with timestamps to support feature extraction and analytics services on the cloud. 

The latest code is on Github: <https://github.com/EricCheung3/Kaleidoscope>.

### Open-source Components and Library ###

* Streaming Server: EasyDarwin streaming server, version used [here](https://bitbucket.org/EricCHeung-admin/easydarwin/src) and now it is maintained by a Chinese team [EasyDarwin on Github](https://github.com/EasyDarwin/EasyDarwin)
* Streaming library: libstreaming is an API that allows you, with only a few lines of code, to stream the camera and/or microphone of an android powered device using RTP over UDP. (origin version on [Github](https://github.com/fyhertz/libstreaming))

* Instant Messaging (XMPP) Server: Openfire server, version used 3.10.2 (new version download from [here](http://www.igniterealtime.org/downloads/index.jsp) or download source code from [source code](http://www.igniterealtime.org/downloads/source.jsp))
* IM library: asmack --an XMPP client library on android, version used is [asmack-2010.12.11.jar](https://code.google.com/archive/p/asmack/downloads) (newest version on [Github](https://github.com/igniterealtime/Smack) or you can download from [here](http://www.igniterealtime.org/downloads/index.jsp#smack))


### Set up for Developer ###

* Environment Setup

* Streaming server installation and configuration

* XMPP server installation and configuration

* Database installation configuration

* Media Streaming storage manual

* Kaleidoscope client installation and configuration

* Deployment instructions

More details on [WiKi document](https://bitbucket.org/EricCHeung-admin/kaleidoscope/wiki/browse/)

### Question Contact ###

* Author: Hu Zhang, Diego Serrano, Eleni Stroulia
* Email: hzhang3(@)ualberta(.)ca, serranos(@)ualberta(.)ca, stroulia(@)ualberta(.)ca
* September 25, 2015
