"use strict";
var api_error = require('./api_error');
(function (ActionType) {
    ActionType[ActionType["NOP"] = 0] = "NOP";
    ActionType[ActionType["THROW_EXCEPTION"] = 1] = "THROW_EXCEPTION";
    ActionType[ActionType["TRUNCATE_FILE"] = 2] = "TRUNCATE_FILE";
    ActionType[ActionType["CREATE_FILE"] = 3] = "CREATE_FILE";
})(exports.ActionType || (exports.ActionType = {}));
var ActionType = exports.ActionType;
var FileFlag = (function () {
    function FileFlag(flagStr) {
        this.flagStr = flagStr;
        if (FileFlag.validFlagStrs.indexOf(flagStr) < 0) {
            throw new api_error.ApiError(api_error.ErrorCode.EINVAL, "Invalid flag: " + flagStr);
        }
    }
    FileFlag.getFileFlag = function (flagStr) {
        if (FileFlag.flagCache.hasOwnProperty(flagStr)) {
            return FileFlag.flagCache[flagStr];
        }
        return FileFlag.flagCache[flagStr] = new FileFlag(flagStr);
    };
    FileFlag.prototype.getFlagString = function () {
        return this.flagStr;
    };
    FileFlag.prototype.isReadable = function () {
        return this.flagStr.indexOf('r') !== -1 || this.flagStr.indexOf('+') !== -1;
    };
    FileFlag.prototype.isWriteable = function () {
        return this.flagStr.indexOf('w') !== -1 || this.flagStr.indexOf('a') !== -1 || this.flagStr.indexOf('+') !== -1;
    };
    FileFlag.prototype.isTruncating = function () {
        return this.flagStr.indexOf('w') !== -1;
    };
    FileFlag.prototype.isAppendable = function () {
        return this.flagStr.indexOf('a') !== -1;
    };
    FileFlag.prototype.isSynchronous = function () {
        return this.flagStr.indexOf('s') !== -1;
    };
    FileFlag.prototype.isExclusive = function () {
        return this.flagStr.indexOf('x') !== -1;
    };
    FileFlag.prototype.pathExistsAction = function () {
        if (this.isExclusive()) {
            return ActionType.THROW_EXCEPTION;
        }
        else if (this.isTruncating()) {
            return ActionType.TRUNCATE_FILE;
        }
        else {
            return ActionType.NOP;
        }
    };
    FileFlag.prototype.pathNotExistsAction = function () {
        if ((this.isWriteable() || this.isAppendable()) && this.flagStr !== 'r+') {
            return ActionType.CREATE_FILE;
        }
        else {
            return ActionType.THROW_EXCEPTION;
        }
    };
    FileFlag.flagCache = {};
    FileFlag.validFlagStrs = ['r', 'r+', 'rs', 'rs+', 'w', 'wx', 'w+', 'wx+', 'a', 'ax', 'a+', 'ax+'];
    return FileFlag;
}());
exports.FileFlag = FileFlag;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZmlsZV9mbGFnLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vc3JjL2NvcmUvZmlsZV9mbGFnLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQSxJQUFPLFNBQVMsV0FBVyxhQUFhLENBQUMsQ0FBQztBQUsxQyxXQUFZLFVBQVU7SUFFcEIseUNBQU8sQ0FBQTtJQUVQLGlFQUFtQixDQUFBO0lBRW5CLDZEQUFpQixDQUFBO0lBRWpCLHlEQUFlLENBQUE7QUFDakIsQ0FBQyxFQVRXLGtCQUFVLEtBQVYsa0JBQVUsUUFTckI7QUFURCxJQUFZLFVBQVUsR0FBVixrQkFTWCxDQUFBO0FBcUJEO0lBMEJFLGtCQUFZLE9BQWU7UUFDekIsSUFBSSxDQUFDLE9BQU8sR0FBRyxPQUFPLENBQUM7UUFDdkIsRUFBRSxDQUFDLENBQUMsUUFBUSxDQUFDLGFBQWEsQ0FBQyxPQUFPLENBQUMsT0FBTyxDQUFDLEdBQUcsQ0FBQyxDQUFDLENBQUMsQ0FBQztZQUNoRCxNQUFNLElBQUksU0FBUyxDQUFDLFFBQVEsQ0FBQyxTQUFTLENBQUMsU0FBUyxDQUFDLE1BQU0sRUFBRSxnQkFBZ0IsR0FBRyxPQUFPLENBQUMsQ0FBQztRQUN2RixDQUFDO0lBQ0gsQ0FBQztJQW5CYSxvQkFBVyxHQUF6QixVQUEwQixPQUFlO1FBRXZDLEVBQUUsQ0FBQyxDQUFDLFFBQVEsQ0FBQyxTQUFTLENBQUMsY0FBYyxDQUFDLE9BQU8sQ0FBQyxDQUFDLENBQUMsQ0FBQztZQUMvQyxNQUFNLENBQUMsUUFBUSxDQUFDLFNBQVMsQ0FBQyxPQUFPLENBQUMsQ0FBQztRQUNyQyxDQUFDO1FBQ0QsTUFBTSxDQUFDLFFBQVEsQ0FBQyxTQUFTLENBQUMsT0FBTyxDQUFDLEdBQUcsSUFBSSxRQUFRLENBQUMsT0FBTyxDQUFDLENBQUM7SUFDN0QsQ0FBQztJQWtCTSxnQ0FBYSxHQUFwQjtRQUNFLE1BQU0sQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDO0lBQ3RCLENBQUM7SUFNTSw2QkFBVSxHQUFqQjtRQUNFLE1BQU0sQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDLE9BQU8sQ0FBQyxHQUFHLENBQUMsS0FBSyxDQUFDLENBQUMsSUFBSSxJQUFJLENBQUMsT0FBTyxDQUFDLE9BQU8sQ0FBQyxHQUFHLENBQUMsS0FBSyxDQUFDLENBQUMsQ0FBQztJQUM5RSxDQUFDO0lBS00sOEJBQVcsR0FBbEI7UUFDRSxNQUFNLENBQUMsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLENBQUMsR0FBRyxDQUFDLEtBQUssQ0FBQyxDQUFDLElBQUksSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLENBQUMsR0FBRyxDQUFDLEtBQUssQ0FBQyxDQUFDLElBQUksSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLENBQUMsR0FBRyxDQUFDLEtBQUssQ0FBQyxDQUFDLENBQUM7SUFDbEgsQ0FBQztJQUtNLCtCQUFZLEdBQW5CO1FBQ0UsTUFBTSxDQUFDLElBQUksQ0FBQyxPQUFPLENBQUMsT0FBTyxDQUFDLEdBQUcsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDO0lBQzFDLENBQUM7SUFLTSwrQkFBWSxHQUFuQjtRQUNFLE1BQU0sQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDLE9BQU8sQ0FBQyxHQUFHLENBQUMsS0FBSyxDQUFDLENBQUMsQ0FBQztJQUMxQyxDQUFDO0lBS00sZ0NBQWEsR0FBcEI7UUFDRSxNQUFNLENBQUMsSUFBSSxDQUFDLE9BQU8sQ0FBQyxPQUFPLENBQUMsR0FBRyxDQUFDLEtBQUssQ0FBQyxDQUFDLENBQUM7SUFDMUMsQ0FBQztJQUtNLDhCQUFXLEdBQWxCO1FBQ0UsTUFBTSxDQUFDLElBQUksQ0FBQyxPQUFPLENBQUMsT0FBTyxDQUFDLEdBQUcsQ0FBQyxLQUFLLENBQUMsQ0FBQyxDQUFDO0lBQzFDLENBQUM7SUFNTSxtQ0FBZ0IsR0FBdkI7UUFDRSxFQUFFLENBQUMsQ0FBQyxJQUFJLENBQUMsV0FBVyxFQUFFLENBQUMsQ0FBQyxDQUFDO1lBQ3ZCLE1BQU0sQ0FBQyxVQUFVLENBQUMsZUFBZSxDQUFDO1FBQ3BDLENBQUM7UUFBQyxJQUFJLENBQUMsRUFBRSxDQUFDLENBQUMsSUFBSSxDQUFDLFlBQVksRUFBRSxDQUFDLENBQUMsQ0FBQztZQUMvQixNQUFNLENBQUMsVUFBVSxDQUFDLGFBQWEsQ0FBQztRQUNsQyxDQUFDO1FBQUMsSUFBSSxDQUFDLENBQUM7WUFDTixNQUFNLENBQUMsVUFBVSxDQUFDLEdBQUcsQ0FBQztRQUN4QixDQUFDO0lBQ0gsQ0FBQztJQU1NLHNDQUFtQixHQUExQjtRQUNFLEVBQUUsQ0FBQyxDQUFDLENBQUMsSUFBSSxDQUFDLFdBQVcsRUFBRSxJQUFJLElBQUksQ0FBQyxZQUFZLEVBQUUsQ0FBQyxJQUFJLElBQUksQ0FBQyxPQUFPLEtBQUssSUFBSSxDQUFDLENBQUMsQ0FBQztZQUN6RSxNQUFNLENBQUMsVUFBVSxDQUFDLFdBQVcsQ0FBQztRQUNoQyxDQUFDO1FBQUMsSUFBSSxDQUFDLENBQUM7WUFDTixNQUFNLENBQUMsVUFBVSxDQUFDLGVBQWUsQ0FBQztRQUNwQyxDQUFDO0lBQ0gsQ0FBQztJQXpHYyxrQkFBUyxHQUFpQyxFQUFFLENBQUM7SUFFN0Msc0JBQWEsR0FBRyxDQUFDLEdBQUcsRUFBRSxJQUFJLEVBQUUsSUFBSSxFQUFFLEtBQUssRUFBRSxHQUFHLEVBQUUsSUFBSSxFQUFFLElBQUksRUFBRSxLQUFLLEVBQUUsR0FBRyxFQUFFLElBQUksRUFBRSxJQUFJLEVBQUUsS0FBSyxDQUFDLENBQUM7SUF3RzFHLGVBQUM7QUFBRCxDQUFDLEFBNUdELElBNEdDO0FBNUdZLGdCQUFRLFdBNEdwQixDQUFBIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IGFwaV9lcnJvciA9IHJlcXVpcmUoJy4vYXBpX2Vycm9yJyk7XHJcblxyXG4vKipcclxuICogQGNsYXNzXHJcbiAqL1xyXG5leHBvcnQgZW51bSBBY3Rpb25UeXBlIHtcclxuICAvLyBJbmRpY2F0ZXMgdGhhdCB0aGUgY29kZSBzaG91bGQgbm90IGRvIGFueXRoaW5nLlxyXG4gIE5PUCA9IDAsXHJcbiAgLy8gSW5kaWNhdGVzIHRoYXQgdGhlIGNvZGUgc2hvdWxkIHRocm93IGFuIGV4Y2VwdGlvbi5cclxuICBUSFJPV19FWENFUFRJT04gPSAxLFxyXG4gIC8vIEluZGljYXRlcyB0aGF0IHRoZSBjb2RlIHNob3VsZCB0cnVuY2F0ZSB0aGUgZmlsZSwgYnV0IG9ubHkgaWYgaXQgaXMgYSBmaWxlLlxyXG4gIFRSVU5DQVRFX0ZJTEUgPSAyLFxyXG4gIC8vIEluZGljYXRlcyB0aGF0IHRoZSBjb2RlIHNob3VsZCBjcmVhdGUgdGhlIGZpbGUuXHJcbiAgQ1JFQVRFX0ZJTEUgPSAzXHJcbn1cclxuXHJcbi8qKlxyXG4gKiBSZXByZXNlbnRzIG9uZSBvZiB0aGUgZm9sbG93aW5nIGZpbGUgZmxhZ3MuIEEgY29udmVuaWVuY2Ugb2JqZWN0LlxyXG4gKlxyXG4gKiAqIGAncidgIC0gT3BlbiBmaWxlIGZvciByZWFkaW5nLiBBbiBleGNlcHRpb24gb2NjdXJzIGlmIHRoZSBmaWxlIGRvZXMgbm90IGV4aXN0LlxyXG4gKiAqIGAncisnYCAtIE9wZW4gZmlsZSBmb3IgcmVhZGluZyBhbmQgd3JpdGluZy4gQW4gZXhjZXB0aW9uIG9jY3VycyBpZiB0aGUgZmlsZSBkb2VzIG5vdCBleGlzdC5cclxuICogKiBgJ3JzJ2AgLSBPcGVuIGZpbGUgZm9yIHJlYWRpbmcgaW4gc3luY2hyb25vdXMgbW9kZS4gSW5zdHJ1Y3RzIHRoZSBmaWxlc3lzdGVtIHRvIG5vdCBjYWNoZSB3cml0ZXMuXHJcbiAqICogYCdycysnYCAtIE9wZW4gZmlsZSBmb3IgcmVhZGluZyBhbmQgd3JpdGluZywgYW5kIG9wZW5zIHRoZSBmaWxlIGluIHN5bmNocm9ub3VzIG1vZGUuXHJcbiAqICogYCd3J2AgLSBPcGVuIGZpbGUgZm9yIHdyaXRpbmcuIFRoZSBmaWxlIGlzIGNyZWF0ZWQgKGlmIGl0IGRvZXMgbm90IGV4aXN0KSBvciB0cnVuY2F0ZWQgKGlmIGl0IGV4aXN0cykuXHJcbiAqICogYCd3eCdgIC0gTGlrZSAndycgYnV0IG9wZW5zIHRoZSBmaWxlIGluIGV4Y2x1c2l2ZSBtb2RlLlxyXG4gKiAqIGAndysnYCAtIE9wZW4gZmlsZSBmb3IgcmVhZGluZyBhbmQgd3JpdGluZy4gVGhlIGZpbGUgaXMgY3JlYXRlZCAoaWYgaXQgZG9lcyBub3QgZXhpc3QpIG9yIHRydW5jYXRlZCAoaWYgaXQgZXhpc3RzKS5cclxuICogKiBgJ3d4KydgIC0gTGlrZSAndysnIGJ1dCBvcGVucyB0aGUgZmlsZSBpbiBleGNsdXNpdmUgbW9kZS5cclxuICogKiBgJ2EnYCAtIE9wZW4gZmlsZSBmb3IgYXBwZW5kaW5nLiBUaGUgZmlsZSBpcyBjcmVhdGVkIGlmIGl0IGRvZXMgbm90IGV4aXN0LlxyXG4gKiAqIGAnYXgnYCAtIExpa2UgJ2EnIGJ1dCBvcGVucyB0aGUgZmlsZSBpbiBleGNsdXNpdmUgbW9kZS5cclxuICogKiBgJ2ErJ2AgLSBPcGVuIGZpbGUgZm9yIHJlYWRpbmcgYW5kIGFwcGVuZGluZy4gVGhlIGZpbGUgaXMgY3JlYXRlZCBpZiBpdCBkb2VzIG5vdCBleGlzdC5cclxuICogKiBgJ2F4KydgIC0gTGlrZSAnYSsnIGJ1dCBvcGVucyB0aGUgZmlsZSBpbiBleGNsdXNpdmUgbW9kZS5cclxuICpcclxuICogRXhjbHVzaXZlIG1vZGUgZW5zdXJlcyB0aGF0IHRoZSBmaWxlIHBhdGggaXMgbmV3bHkgY3JlYXRlZC5cclxuICogQGNsYXNzXHJcbiAqL1xyXG5leHBvcnQgY2xhc3MgRmlsZUZsYWcge1xyXG4gIC8vIENvbnRhaW5zIGNhY2hlZCBGaWxlTW9kZSBpbnN0YW5jZXMuXHJcbiAgcHJpdmF0ZSBzdGF0aWMgZmxhZ0NhY2hlOiB7IFttb2RlOiBzdHJpbmddOiBGaWxlRmxhZyB9ID0ge307XHJcbiAgLy8gQXJyYXkgb2YgdmFsaWQgbW9kZSBzdHJpbmdzLlxyXG4gIHByaXZhdGUgc3RhdGljIHZhbGlkRmxhZ1N0cnMgPSBbJ3InLCAncisnLCAncnMnLCAncnMrJywgJ3cnLCAnd3gnLCAndysnLCAnd3grJywgJ2EnLCAnYXgnLCAnYSsnLCAnYXgrJ107XHJcblxyXG4gIC8qKlxyXG4gICAqIEdldCBhbiBvYmplY3QgcmVwcmVzZW50aW5nIHRoZSBnaXZlbiBmaWxlIG1vZGUuXHJcbiAgICogQHBhcmFtIFtTdHJpbmddIG1vZGVTdHIgVGhlIHN0cmluZyByZXByZXNlbnRpbmcgdGhlIG1vZGVcclxuICAgKiBAcmV0dXJuIFtCcm93c2VyRlMuRmlsZU1vZGVdIFRoZSBGaWxlTW9kZSBvYmplY3QgcmVwcmVzZW50aW5nIHRoZSBtb2RlXHJcbiAgICogQHRocm93IFtCcm93c2VyRlMuQXBpRXJyb3JdIHdoZW4gdGhlIG1vZGUgc3RyaW5nIGlzIGludmFsaWRcclxuICAgKi9cclxuICBwdWJsaWMgc3RhdGljIGdldEZpbGVGbGFnKGZsYWdTdHI6IHN0cmluZyk6IEZpbGVGbGFnIHtcclxuICAgIC8vIENoZWNrIGNhY2hlIGZpcnN0LlxyXG4gICAgaWYgKEZpbGVGbGFnLmZsYWdDYWNoZS5oYXNPd25Qcm9wZXJ0eShmbGFnU3RyKSkge1xyXG4gICAgICByZXR1cm4gRmlsZUZsYWcuZmxhZ0NhY2hlW2ZsYWdTdHJdO1xyXG4gICAgfVxyXG4gICAgcmV0dXJuIEZpbGVGbGFnLmZsYWdDYWNoZVtmbGFnU3RyXSA9IG5ldyBGaWxlRmxhZyhmbGFnU3RyKTtcclxuICB9XHJcblxyXG4gIHByaXZhdGUgZmxhZ1N0cjogc3RyaW5nO1xyXG4gIC8qKlxyXG4gICAqIFRoaXMgc2hvdWxkIG5ldmVyIGJlIGNhbGxlZCBkaXJlY3RseS5cclxuICAgKiBAcGFyYW0gW1N0cmluZ10gbW9kZVN0ciBUaGUgc3RyaW5nIHJlcHJlc2VudGluZyB0aGUgbW9kZVxyXG4gICAqIEB0aHJvdyBbQnJvd3NlckZTLkFwaUVycm9yXSB3aGVuIHRoZSBtb2RlIHN0cmluZyBpcyBpbnZhbGlkXHJcbiAgICovXHJcbiAgY29uc3RydWN0b3IoZmxhZ1N0cjogc3RyaW5nKSB7XHJcbiAgICB0aGlzLmZsYWdTdHIgPSBmbGFnU3RyO1xyXG4gICAgaWYgKEZpbGVGbGFnLnZhbGlkRmxhZ1N0cnMuaW5kZXhPZihmbGFnU3RyKSA8IDApIHtcclxuICAgICAgdGhyb3cgbmV3IGFwaV9lcnJvci5BcGlFcnJvcihhcGlfZXJyb3IuRXJyb3JDb2RlLkVJTlZBTCwgXCJJbnZhbGlkIGZsYWc6IFwiICsgZmxhZ1N0cik7XHJcbiAgICB9XHJcbiAgfVxyXG5cclxuICAvKipcclxuICAgKiBHZXQgdGhlIHVuZGVybHlpbmcgZmxhZyBzdHJpbmcgZm9yIHRoaXMgZmxhZy5cclxuICAgKi9cclxuICBwdWJsaWMgZ2V0RmxhZ1N0cmluZygpOiBzdHJpbmcge1xyXG4gICAgcmV0dXJuIHRoaXMuZmxhZ1N0cjtcclxuICB9XHJcblxyXG4gIC8qKlxyXG4gICAqIFJldHVybnMgdHJ1ZSBpZiB0aGUgZmlsZSBpcyByZWFkYWJsZS5cclxuICAgKiBAcmV0dXJuIFtCb29sZWFuXVxyXG4gICAqL1xyXG4gIHB1YmxpYyBpc1JlYWRhYmxlKCk6IGJvb2xlYW4ge1xyXG4gICAgcmV0dXJuIHRoaXMuZmxhZ1N0ci5pbmRleE9mKCdyJykgIT09IC0xIHx8IHRoaXMuZmxhZ1N0ci5pbmRleE9mKCcrJykgIT09IC0xO1xyXG4gIH1cclxuICAvKipcclxuICAgKiBSZXR1cm5zIHRydWUgaWYgdGhlIGZpbGUgaXMgd3JpdGVhYmxlLlxyXG4gICAqIEByZXR1cm4gW0Jvb2xlYW5dXHJcbiAgICovXHJcbiAgcHVibGljIGlzV3JpdGVhYmxlKCk6IGJvb2xlYW4ge1xyXG4gICAgcmV0dXJuIHRoaXMuZmxhZ1N0ci5pbmRleE9mKCd3JykgIT09IC0xIHx8IHRoaXMuZmxhZ1N0ci5pbmRleE9mKCdhJykgIT09IC0xIHx8IHRoaXMuZmxhZ1N0ci5pbmRleE9mKCcrJykgIT09IC0xO1xyXG4gIH1cclxuICAvKipcclxuICAgKiBSZXR1cm5zIHRydWUgaWYgdGhlIGZpbGUgbW9kZSBzaG91bGQgdHJ1bmNhdGUuXHJcbiAgICogQHJldHVybiBbQm9vbGVhbl1cclxuICAgKi9cclxuICBwdWJsaWMgaXNUcnVuY2F0aW5nKCk6IGJvb2xlYW4ge1xyXG4gICAgcmV0dXJuIHRoaXMuZmxhZ1N0ci5pbmRleE9mKCd3JykgIT09IC0xO1xyXG4gIH1cclxuICAvKipcclxuICAgKiBSZXR1cm5zIHRydWUgaWYgdGhlIGZpbGUgaXMgYXBwZW5kYWJsZS5cclxuICAgKiBAcmV0dXJuIFtCb29sZWFuXVxyXG4gICAqL1xyXG4gIHB1YmxpYyBpc0FwcGVuZGFibGUoKTogYm9vbGVhbiB7XHJcbiAgICByZXR1cm4gdGhpcy5mbGFnU3RyLmluZGV4T2YoJ2EnKSAhPT0gLTE7XHJcbiAgfVxyXG4gIC8qKlxyXG4gICAqIFJldHVybnMgdHJ1ZSBpZiB0aGUgZmlsZSBpcyBvcGVuIGluIHN5bmNocm9ub3VzIG1vZGUuXHJcbiAgICogQHJldHVybiBbQm9vbGVhbl1cclxuICAgKi9cclxuICBwdWJsaWMgaXNTeW5jaHJvbm91cygpOiBib29sZWFuIHtcclxuICAgIHJldHVybiB0aGlzLmZsYWdTdHIuaW5kZXhPZigncycpICE9PSAtMTtcclxuICB9XHJcbiAgLyoqXHJcbiAgICogUmV0dXJucyB0cnVlIGlmIHRoZSBmaWxlIGlzIG9wZW4gaW4gZXhjbHVzaXZlIG1vZGUuXHJcbiAgICogQHJldHVybiBbQm9vbGVhbl1cclxuICAgKi9cclxuICBwdWJsaWMgaXNFeGNsdXNpdmUoKTogYm9vbGVhbiB7XHJcbiAgICByZXR1cm4gdGhpcy5mbGFnU3RyLmluZGV4T2YoJ3gnKSAhPT0gLTE7XHJcbiAgfVxyXG4gIC8qKlxyXG4gICAqIFJldHVybnMgb25lIG9mIHRoZSBzdGF0aWMgZmllbGRzIG9uIHRoaXMgb2JqZWN0IHRoYXQgaW5kaWNhdGVzIHRoZVxyXG4gICAqIGFwcHJvcHJpYXRlIHJlc3BvbnNlIHRvIHRoZSBwYXRoIGV4aXN0aW5nLlxyXG4gICAqIEByZXR1cm4gW051bWJlcl1cclxuICAgKi9cclxuICBwdWJsaWMgcGF0aEV4aXN0c0FjdGlvbigpOiBBY3Rpb25UeXBlIHtcclxuICAgIGlmICh0aGlzLmlzRXhjbHVzaXZlKCkpIHtcclxuICAgICAgcmV0dXJuIEFjdGlvblR5cGUuVEhST1dfRVhDRVBUSU9OO1xyXG4gICAgfSBlbHNlIGlmICh0aGlzLmlzVHJ1bmNhdGluZygpKSB7XHJcbiAgICAgIHJldHVybiBBY3Rpb25UeXBlLlRSVU5DQVRFX0ZJTEU7XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICByZXR1cm4gQWN0aW9uVHlwZS5OT1A7XHJcbiAgICB9XHJcbiAgfVxyXG4gIC8qKlxyXG4gICAqIFJldHVybnMgb25lIG9mIHRoZSBzdGF0aWMgZmllbGRzIG9uIHRoaXMgb2JqZWN0IHRoYXQgaW5kaWNhdGVzIHRoZVxyXG4gICAqIGFwcHJvcHJpYXRlIHJlc3BvbnNlIHRvIHRoZSBwYXRoIG5vdCBleGlzdGluZy5cclxuICAgKiBAcmV0dXJuIFtOdW1iZXJdXHJcbiAgICovXHJcbiAgcHVibGljIHBhdGhOb3RFeGlzdHNBY3Rpb24oKTogQWN0aW9uVHlwZSB7XHJcbiAgICBpZiAoKHRoaXMuaXNXcml0ZWFibGUoKSB8fCB0aGlzLmlzQXBwZW5kYWJsZSgpKSAmJiB0aGlzLmZsYWdTdHIgIT09ICdyKycpIHtcclxuICAgICAgcmV0dXJuIEFjdGlvblR5cGUuQ1JFQVRFX0ZJTEU7XHJcbiAgICB9IGVsc2Uge1xyXG4gICAgICByZXR1cm4gQWN0aW9uVHlwZS5USFJPV19FWENFUFRJT047XHJcbiAgICB9XHJcbiAgfVxyXG59XHJcbiJdfQ==