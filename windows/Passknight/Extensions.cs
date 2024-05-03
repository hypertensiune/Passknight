using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Passknight.Extensions
{
    static class Extensions
    {
        // https://stackoverflow.com/questions/41171514/what-is-the-task-equivalent-to-promise-then
        // https://github.com/dotnet/runtime/issues/58692
        public static async Task Then(this Task task, Action then)
        {
            await task;
            then();
        }

        public static async Task Then<T>(this Task<T> task, Action<T> then)
        {
            var res = await task;
            then(res);
        }

        public static async Task<V> Then<T, V>(this Task<T> task, Func<T, V> then)
        {
            var res = await task;
            return then(res);
        }
    }
}
