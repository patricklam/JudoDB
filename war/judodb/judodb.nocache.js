function judodb(){var P='',wb='" for "gwt:onLoadErrorFn"',ub='" for "gwt:onPropertyErrorFn"',ib='"><\/script>',Z='#',Eb='&',pc='.cache.html',_='/',Wb='0A80B877FF92085A927782B7749187F7',Yb='1E31D4D74F0155955CD1C8E183E5199D',Zb='25EFD5956D6B230424FF1E4142D1C2EF',$b='2619E2E30D92D91DC588AE9FF63A9C18',_b='2983DE3DA7A7C28130A106758B1C076D',bc='3BAE5CF579D9BCF42216561AAD523CE0',cc='48D46303B6730BE4FA34D8900DCD8F45',dc='66CC76973368361BAE8CD5BB7CA5420F',ec='74ADD638D0C90C18C41F4D10A0A2D3D2',fc='7AC9B047F216066C6F93584DA046879B',gc='8813C5A68D4A5F7F76382F60D01F5F16',hc='8D599B506A3DEEBF9A3E159820A22BF7',ic='9376C9B1B3523794EC209BF8EB173B12',jc='99D5F7C848709659B8E676B92030AC1A',oc=':',ob='::',yc='<script defer="defer">judodb.onInjectionDone(\'judodb\')<\/script>',hb='<script id="',rb='=',$='?',kc='B1DD9FE13B4EFB3DB64BA306FCD33F57',lc='B8514EE3644EB1287D9DEAAC225D82B8',mc='BC08A08C1C94D1A37860B836B8857EE5',tb='Bad handler "',xc='DOMContentLoaded',nc='FB6F49E9E86C50285AA30B8D4847BBCC',wc='JudoDB.css',jb='SCRIPT',Hb='Unexpected exception in locale detection, using default: ',Gb='_',Fb='__gwt_Locale',gb='__gwt_marker_judodb',kb='base',cb='baseUrl',T='begin',S='bootstrap',bb='clear.cache.gif',qb='content',Cb='default',Y='end',ac='fr',Xb='fr_CA',Qb='gecko',Rb='gecko1_8',U='gwt.codesvr=',V='gwt.hosted=',W='gwt.hybrid',qc='gwt/clean/clean.css',vb='gwt:onLoadErrorFn',sb='gwt:onPropertyErrorFn',pb='gwt:property',vc='head',Ub='hosted.html?judodb',uc='href',Pb='ie6',Ob='ie8',Nb='ie9',xb='iframe',ab='img',yb="javascript:''",Q='judodb',eb='judodb.nocache.js',nb='judodb::',rc='link',Tb='loadExternalRefs',Bb='locale',Db='locale=',lb='meta',Ab='moduleRequested',X='moduleStartup',Mb='msie',mb='name',Jb='opera',zb='position:absolute;width:0;height:0;border:none',sc='rel',Lb='safari',db='script',Vb='selectingPermutation',R='startup',tc='stylesheet',fb='undefined',Sb='unknown',Ib='user.agent',Kb='webkit';var l=window,m=document,n=l.__gwtStatsEvent?function(a){return l.__gwtStatsEvent(a)}:null,o=l.__gwtStatsSessionId?l.__gwtStatsSessionId:null,p,q,r,s=P,t={},u=[],v=[],w=[],x=0,y,z;n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:T});if(!l.__gwt_stylesLoaded){l.__gwt_stylesLoaded={}}if(!l.__gwt_scriptsLoaded){l.__gwt_scriptsLoaded={}}function A(){var b=false;try{var c=l.location.search;return (c.indexOf(U)!=-1||(c.indexOf(V)!=-1||l.external&&l.external.gwtOnLoad))&&c.indexOf(W)==-1}catch(a){}A=function(){return b};return b}
function B(){if(p&&q){var b=m.getElementById(Q);var c=b.contentWindow;if(A()){c.__gwt_getProperty=function(a){return H(a)}}judodb=null;c.gwtOnLoad(y,Q,s,x);n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:X,millis:(new Date).getTime(),type:Y})}}
function C(){function e(a){var b=a.lastIndexOf(Z);if(b==-1){b=a.length}var c=a.indexOf($);if(c==-1){c=a.length}var d=a.lastIndexOf(_,Math.min(c,b));return d>=0?a.substring(0,d+1):P}
function f(a){if(a.match(/^\w+:\/\//)){}else{var b=m.createElement(ab);b.src=a+bb;a=e(b.src)}return a}
function g(){var a=F(cb);if(a!=null){return a}return P}
function h(){var a=m.getElementsByTagName(db);for(var b=0;b<a.length;++b){if(a[b].src.indexOf(eb)!=-1){return e(a[b].src)}}return P}
function i(){var a;if(typeof isBodyLoaded==fb||!isBodyLoaded()){var b=gb;var c;m.write(hb+b+ib);c=m.getElementById(b);a=c&&c.previousSibling;while(a&&a.tagName!=jb){a=a.previousSibling}if(c){c.parentNode.removeChild(c)}if(a&&a.src){return e(a.src)}}return P}
function j(){var a=m.getElementsByTagName(kb);if(a.length>0){return a[a.length-1].href}return P}
var k=g();if(k==P){k=h()}if(k==P){k=i()}if(k==P){k=j()}if(k==P){k=e(m.location.href)}k=f(k);s=k;return k}
function D(){var b=document.getElementsByTagName(lb);for(var c=0,d=b.length;c<d;++c){var e=b[c],f=e.getAttribute(mb),g;if(f){f=f.replace(nb,P);if(f.indexOf(ob)>=0){continue}if(f==pb){g=e.getAttribute(qb);if(g){var h,i=g.indexOf(rb);if(i>=0){f=g.substring(0,i);h=g.substring(i+1)}else{f=g;h=P}t[f]=h}}else if(f==sb){g=e.getAttribute(qb);if(g){try{z=eval(g)}catch(a){alert(tb+g+ub)}}}else if(f==vb){g=e.getAttribute(qb);if(g){try{y=eval(g)}catch(a){alert(tb+g+wb)}}}}}}
function E(a,b){return b in u[a]}
function F(a){var b=t[a];return b==null?null:b}
function G(a,b){var c=w;for(var d=0,e=a.length-1;d<e;++d){c=c[a[d]]||(c[a[d]]=[])}c[a[e]]=b}
function H(a){var b=v[a](),c=u[a];if(b in c){return b}var d=[];for(var e in c){d[c[e]]=e}if(z){z(a,d,b)}throw null}
var I;function J(){if(!I){I=true;var a=m.createElement(xb);a.src=yb;a.id=Q;a.style.cssText=zb;a.tabIndex=-1;m.body.appendChild(a);n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:X,millis:(new Date).getTime(),type:Ab});a.contentWindow.location.replace(s+L)}}
v[Bb]=function(){var b=null;var c=Cb;try{if(!b){var d=location.search;var e=d.indexOf(Db);if(e>=0){var f=d.substring(e+7);var g=d.indexOf(Eb,e);if(g<0){g=d.length}b=d.substring(e+7,g)}}if(!b){b=F(Bb)}if(!b){b=l[Fb]}if(b){c=b}while(b&&!E(Bb,b)){var h=b.lastIndexOf(Gb);if(h<0){b=null;break}b=b.substring(0,h)}}catch(a){alert(Hb+a)}l[Fb]=c;return b||Cb};u[Bb]={'default':0,fr:1,fr_CA:2};v[Ib]=function(){var b=navigator.userAgent.toLowerCase();var c=function(a){return parseInt(a[1])*1000+parseInt(a[2])};if(function(){return b.indexOf(Jb)!=-1}())return Jb;if(function(){return b.indexOf(Kb)!=-1}())return Lb;if(function(){return b.indexOf(Mb)!=-1&&m.documentMode>=9}())return Nb;if(function(){return b.indexOf(Mb)!=-1&&m.documentMode>=8}())return Ob;if(function(){var a=/msie ([0-9]+)\.([0-9]+)/.exec(b);if(a&&a.length==3)return c(a)>=6000}())return Pb;if(function(){return b.indexOf(Qb)!=-1}())return Rb;return Sb};u[Ib]={gecko1_8:0,ie6:1,ie8:2,ie9:3,opera:4,safari:5};judodb.onScriptLoad=function(){if(I){q=true;B()}};judodb.onInjectionDone=function(){p=true;n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:Tb,millis:(new Date).getTime(),type:Y});B()};D();C();var K;var L;if(A()){if(l.external&&(l.external.initModule&&l.external.initModule(Q))){l.location.reload();return}L=Ub;K=P}n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:Vb});if(!A()){try{G([Cb,Nb],Wb);G([Xb,Ob],Yb);G([Xb,Lb],Zb);G([Cb,Lb],$b);G([Xb,Rb],_b);G([ac,Ob],bc);G([Cb,Pb],cc);G([ac,Nb],dc);G([ac,Pb],ec);G([Cb,Rb],fc);G([Xb,Jb],gc);G([Cb,Jb],hc);G([ac,Lb],ic);G([ac,Jb],jc);G([ac,Rb],kc);G([Xb,Pb],lc);G([Xb,Nb],mc);G([Cb,Ob],nc);K=w[H(Bb)][H(Ib)];var M=K.indexOf(oc);if(M!=-1){x=Number(K.substring(M+1));K=K.substring(0,M)}L=K+pc}catch(a){return}}var N;function O(){if(!r){r=true;if(!__gwt_stylesLoaded[qc]){var a=m.createElement(rc);__gwt_stylesLoaded[qc]=a;a.setAttribute(sc,tc);a.setAttribute(uc,s+qc);m.getElementsByTagName(vc)[0].appendChild(a)}if(!__gwt_stylesLoaded[wc]){var a=m.createElement(rc);__gwt_stylesLoaded[wc]=a;a.setAttribute(sc,tc);a.setAttribute(uc,s+wc);m.getElementsByTagName(vc)[0].appendChild(a)}B();if(m.removeEventListener){m.removeEventListener(xc,O,false)}if(N){clearInterval(N)}}}
if(m.addEventListener){m.addEventListener(xc,function(){J();O()},false)}var N=setInterval(function(){if(/loaded|complete/.test(m.readyState)){J();O()}},50);n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:S,millis:(new Date).getTime(),type:Y});n&&n({moduleName:Q,sessionId:o,subSystem:R,evtGroup:Tb,millis:(new Date).getTime(),type:T});m.write(yc)}
judodb();