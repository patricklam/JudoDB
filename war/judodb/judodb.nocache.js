function judodb(){var Q='',yb='" for "gwt:onLoadErrorFn"',wb='" for "gwt:onPropertyErrorFn"',jb='"><\/script>',$='#',Gb='&',rc='.cache.html',ab='/',mb='//',Zb='05F7DBE0309E969996D5E72B7EEF663C',_b='07E7AE6CCE1E97CEE3D2D320CEA6387D',ac='20573588A2DEEEB88A7D7B81AC793CDE',bc='2CF950E8269A915A621F9A5CAB507B59',cc='3E612C5B3CDE82A256BD661F4060DD72',dc='487A3E8670C4C26ED869908770B4A441',ec='4A12B2A1D5569916BF44FB16F685C0EC',fc='573D07459D677E8803DBFEEE462C4E8B',gc='822B4270996DB3646CCF254B4D882A39',hc='8802EFF6146A0F0A753783322761F6ED',ic='8D0B8CFF942778D6D8AB21DA66044B2E',jc='9F2B73161135CDE72E8A40EC3210A69B',qc=':',qb='::',Ac='<script defer="defer">judodb.onInjectionDone(\'judodb\')<\/script>',ib='<script id="',tb='=',_='?',kc='AC47101C4E5FEFCE19137441332EF7E4',lc='AE03FEB0BAF9B6BCA4C5C68A7F1FB8AA',mc='BF610BFE2E382930C621018CD420EE16',vb='Bad handler "',nc='CD65019940AFED2A2D091E7B46981A75',zc='DOMContentLoaded',oc='EA8BBABE26A0F83B3523F4FB03A01214',pc='F13ADA7956FE3536F8DFBF5C25F7BB03',yc='JudoDB.css',kb='SCRIPT',Jb='Unexpected exception in locale detection, using default: ',Ib='_',Hb='__gwt_Locale',hb='__gwt_marker_judodb',lb='base',db='baseUrl',U='begin',T='bootstrap',cb='clear.cache.gif',sb='content',Eb='default',Z='end',$b='fr',Yb='fr_CA',Sb='gecko',Tb='gecko1_8',V='gwt.codesvr=',W='gwt.hosted=',X='gwt.hybrid',sc='gwt/clean/clean.css',xb='gwt:onLoadErrorFn',ub='gwt:onPropertyErrorFn',rb='gwt:property',xc='head',Wb='hosted.html?judodb',wc='href',Rb='ie6',Qb='ie8',Pb='ie9',zb='iframe',bb='img',Ab="javascript:''",R='judodb',fb='judodb.nocache.js',pb='judodb::',tc='link',Vb='loadExternalRefs',Db='locale',Fb='locale=',nb='meta',Cb='moduleRequested',Y='moduleStartup',Ob='msie',ob='name',Lb='opera',Bb='position:absolute;width:0;height:0;border:none',uc='rel',Nb='safari',eb='script',Xb='selectingPermutation',S='startup',vc='stylesheet',gb='undefined',Ub='unknown',Kb='user.agent',Mb='webkit';var m=window,n=document,o=m.__gwtStatsEvent?function(a){return m.__gwtStatsEvent(a)}:null,p=m.__gwtStatsSessionId?m.__gwtStatsSessionId:null,q,r,s,t=Q,u={},v=[],w=[],x=[],y=0,z,A;o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:T,millis:(new Date).getTime(),type:U});if(!m.__gwt_stylesLoaded){m.__gwt_stylesLoaded={}}if(!m.__gwt_scriptsLoaded){m.__gwt_scriptsLoaded={}}function B(){var b=false;try{var c=m.location.search;return (c.indexOf(V)!=-1||(c.indexOf(W)!=-1||m.external&&m.external.gwtOnLoad))&&c.indexOf(X)==-1}catch(a){}B=function(){return b};return b}
function C(){if(q&&r){var b=n.getElementById(R);var c=b.contentWindow;if(B()){c.__gwt_getProperty=function(a){return I(a)}}judodb=null;c.gwtOnLoad(z,R,t,y);o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:Y,millis:(new Date).getTime(),type:Z})}}
function D(){function e(a){var b=a.lastIndexOf($);if(b==-1){b=a.length}var c=a.indexOf(_);if(c==-1){c=a.length}var d=a.lastIndexOf(ab,Math.min(c,b));return d>=0?a.substring(0,d+1):Q}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=n.createElement(bb);b.src=a+cb;a=e(b.src)}return a}
function g(){var a=G(db);if(a!=null){return a}return Q}
function h(){var a=n.getElementsByTagName(eb);for(var b=0;b<a.length;++b){if(a[b].src.indexOf(fb)!=-1){return e(a[b].src)}}return Q}
function i(){var a;if(typeof isBodyLoaded==gb||!isBodyLoaded()){var b=hb;var c;n.write(ib+b+jb);c=n.getElementById(b);a=c&&c.previousSibling;while(a&&a.tagName!=kb){a=a.previousSibling}if(c){c.parentNode.removeChild(c)}if(a&&a.src){return e(a.src)}}return Q}
function j(){var a=n.getElementsByTagName(lb);if(a.length>0){return a[a.length-1].href}return Q}
function k(){var a=n.location;return a.href==a.protocol+mb+a.host+a.pathname+a.search+a.hash}
var l=g();if(l==Q){l=h()}if(l==Q){l=i()}if(l==Q){l=j()}if(l==Q&&k()){l=e(n.location.href)}l=f(l);t=l;return l}
function E(){var b=document.getElementsByTagName(nb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(ob),g;if(f){f=f.replace(pb,Q);if(f.indexOf(qb)>=0){continue}if(f==rb){g=e.getAttribute(sb);if(g){var h,i=g.indexOf(tb);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=Q}u[f]=h}}else if(f==ub){g=e.getAttribute(sb);if(g){try{A=eval(g)}catch(a){alert(vb+g+wb)}}}else if(f==xb){g=e.getAttribute(sb);if(g){try{z=eval(g)}catch(a){alert(vb+g+yb)}}}}}}
function F(a,b){return b in v[a]}
function G(a){var b=u[a];return b==null?null:b}
function H(a,b){var c=x;for(var d=0,e=a.length-1;d<e;++d){c=c[a[d]]||(c[a[d]]=[])}c[a[e]]=b}
function I(a){var b=w[a](),c=v[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(A){A(a,d,b)}throw null}
var J;function K(){if(!J){J=true;var a=n.createElement(zb);a.src=Ab;a.id=R;a.style.cssText=Bb;a.tabIndex=-1;n.body.appendChild(a);o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:Y,millis:(new Date).getTime(),type:Cb});a.contentWindow.location.replace(t+M)}}
w[Db]=function(){var b=null;var c=Eb;try{if(!b){var d=location.search;var e=d.indexOf(Fb);if(e>=0){var f=d.substring(e+7);var g=d.indexOf(Gb,e);if(g<0){g=d.length}b=d.substring(e+7,g)}}if(!b){b=G(Db)}if(!b){b=m[Hb]}if(b){c=b}while(b&&!F(Db,b)){var h=b.lastIndexOf(Ib);if(h<0){b=null;break}b=b.substring(0,h)}}catch(a){alert(Jb+a)}m[Hb]=c;return b||Eb};v[Db]={'default':0,fr:1,fr_CA:2};w[Kb]=function(){var b=navigator.userAgent.toLowerCase();var c=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(function(){return b.indexOf(Lb)!=-1}())return Lb;if(function(){return b.indexOf(Mb)!=-1}())return Nb;if(function(){return b.indexOf(Ob)!=-1&&n.documentMode>=9}())return Pb;if(function(){return b.indexOf(Ob)!=-1&&n.documentMode>=8}())return Qb;if(function(){var a=/msie ([0-9]+)\.([0-9]+)/.exec(b);if(a&&a.length==3)return c(a)>=6000}())return Rb;if(function(){return b.indexOf(Sb)!=-1}())return Tb;return Ub};v[Kb]={gecko1_8:0,ie6:1,ie8:2,ie9:3,opera:4,safari:5};judodb.onScriptLoad=function(){if(J){r=true;C()}};judodb.onInjectionDone=function(){q=true;o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:Vb,millis:(new Date).getTime(),type:Z});C()};E();D();var L;var M;if(B()){if(m.external&&(m.external.initModule&&m.external.initModule(R))){m.location.reload();return}M=Wb;L=Q}o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:T,millis:(new Date).getTime(),type:Xb});if(!B()){try{H([Yb,Qb],Zb);H([$b,Rb],_b);H([$b,Qb],ac);H([Eb,Pb],bc);H([$b,Tb],cc);H([Yb,Nb],dc);H([Yb,Lb],ec);H([Eb,Nb],fc);H([Yb,Tb],gc);H([$b,Lb],hc);H([$b,Pb],ic);H([Yb,Pb],jc);H([Yb,Rb],kc);H([Eb,Rb],lc);H([Eb,Qb],mc);H([$b,Nb],nc);H([Eb,Lb],oc);H([Eb,Tb],pc);L=x[I(Db)][I(Kb)];var N=L.indexOf(qc);if(N!=-1){y=Number(L.substring(N+1));L=L.substring(0,N)}M=L+rc}catch(a){return}}var O;function P(){if(!s){s=true;if(!__gwt_stylesLoaded[sc]){var a=n.createElement(tc);__gwt_stylesLoaded[sc]=a;a.setAttribute(uc,vc);a.setAttribute(wc,t+sc);n.getElementsByTagName(xc)[0].appendChild(a)}if(!__gwt_stylesLoaded[yc]){var a=n.createElement(tc);__gwt_stylesLoaded[yc]=a;a.setAttribute(uc,vc);a.setAttribute(wc,t+yc);n.getElementsByTagName(xc)[0].appendChild(a)}C();if(n.removeEventListener){n.removeEventListener(zc,P,false)}if(O){clearInterval(O)}}}
if(n.addEventListener){n.addEventListener(zc,function(){K();P()},false)}var O=setInterval(function(){if(/loaded|complete/.test(n.readyState)){K();P()}},50);o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:T,millis:(new Date).getTime(),type:Z});o&&o({moduleName:R,sessionId:p,subSystem:S,evtGroup:Vb,millis:(new Date).getTime(),type:U});n.write(Ac)}
judodb();