$wnd.showcase.runAsyncCallback18("function Bz(){}\nfunction KIb(a,b){$w(a.a,b)}\nfunction kfb(a,b){this.b=a;this.a=b}\nfunction mfb(a,b){this.b=a;this.a=b}\nfunction _z(a){return IX(Mz,a)}\nfunction Az(){Az=UW;zz=new Bz}\nfunction efb(a,b){VBb(b,'Selected: '+a.Jf()+', '+a.Kf())}\nfunction WIb(){SIb();VIb.call(this,Mo($doc,'password'),'gwt-PasswordTextBox')}\nfunction zQb(b){try{var c=b.document.selection.createRange();if(c.parentElement()!==b)return 0;return c.text.length}catch(a){return 0}}\nfunction yQb(b){try{var c=b.document.selection.createRange();if(c.parentElement()!==b)return -1;return -c.move(hbc,-65535)}catch(a){return 0}}\nfunction cfb(a,b){var c,d;c=new OFb;c.e[z9b]=4;LFb(c,a);if(b){d=new ZBb('Selected: 0, 0');Hh(a,new kfb(a,d),(iu(),iu(),hu));Hh(a,new mfb(a,d),(Nt(),Nt(),Mt));LFb(c,d)}return c}\nfunction BQb(b){try{var c=b.document.selection.createRange();if(c.parentElement()!==b)return 0;var d=c.text.length;var e=0;var f=c.duplicate();f.moveEnd(hbc,-1);var g=f.text.length;while(g==d&&f.parentElement()==b&&c.compareEndPoints('StartToEnd',f)<=0){e+=2;f.moveEnd(hbc,-1);g=f.text.length}return d+e}catch(a){return 0}}\nfunction AQb(b){try{var c=b.document.selection.createRange();if(c.parentElement()!==b)return -1;var d=c.duplicate();d.moveToElementText(b);d.setEndPoint('EndToStart',c);var e=d.text.length;var f=0;var g=d.duplicate();g.moveEnd(hbc,-1);var h=g.text.length;while(h==e&&g.parentElement()==b){f+=2;g.moveEnd(hbc,-1);h=g.text.length}return e+f}catch(a){return 0}}\nfunction dfb(){var a,b,c,d,e,f;f=new sPb;f.e[z9b]=5;d=new UIb;bPb(d.hb,'','cwBasicText-textbox');KIb(d,(Az(),Az(),zz));b=new UIb;bPb(b.hb,'','cwBasicText-textbox-disabled');b.hb[qac]='read only';Zw(b.a);b.hb[s8b]=true;pPb(f,new cCb('<b>Normal text box:<\\/b>'));pPb(f,cfb(d,true));pPb(f,cfb(b,false));c=new WIb;bPb(c.hb,'','cwBasicText-password');a=new WIb;bPb(a.hb,'','cwBasicText-password-disabled');a.hb[qac]='read only';Zw(a.a);a.hb[s8b]=true;pPb(f,new cCb('<br><br><b>Password text box:<\\/b>'));pPb(f,cfb(c,true));pPb(f,cfb(a,false));e=new wNb;bPb(e.hb,'','cwBasicText-textarea');e.hb.rows=5;pPb(f,new cCb('<br><br><b>Text area:<\\/b>'));pPb(f,cfb(e,true));return f}\nvar hbc='character';TW(865,1132,{},Bz);_.dd=function Cz(a){return _z((Vz(),a))?(Iy(),Hy):(Iy(),Gy)};var zz;var zH=SVb(U6b,'AnyRtlDirectionEstimator',865);TW(436,1,f8b);_.Bc=function jfb(){lZ(this.a,dfb())};TW(437,1,gbc,kfb);_.Uc=function lfb(a){efb(this.b,this.a)};var YM=SVb(p8b,'CwBasicText/2',437);TW(438,1,Y7b,mfb);_.Sc=function nfb(a){efb(this.b,this.a)};var ZM=SVb(p8b,'CwBasicText/3',438);TW(744,245,$5b);_.Jf=function NIb(){return yQb(this.hb)};_.Kf=function OIb(){return zQb(this.hb)};TW(322,50,$5b,WIb);var RR=SVb(Y5b,'PasswordTextBox',322);TW(214,308,$5b);_.Jf=function xNb(){return AQb(this.hb)};_.Kf=function yNb(){return BQb(this.hb)};i5b(wl)(18);\n//# sourceURL=showcase-18.js\n")