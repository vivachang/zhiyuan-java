local current = redis.call('GET', KEYS[1])
local k = ARGV[1]
if current ~= false then
    if cjson.decode(current)[k] == ARGV[2] then
        redis.call('DEL', KEYS[1])
        return true
    end
end

return false